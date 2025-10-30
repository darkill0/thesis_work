package org.example.restapi.api.git;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@RestController
@RequestMapping("/api/git")
public class GitController {

    private final String baseGitRepo = System.getProperty("user.dir") + File.separator + "repos" + File.separator;

    private final String createRepository_Path = "/create/{name}";
    private final String addFileToRepository_Path = "/{name}/add";
    private final String commitToRepository_Path = "/{name}/commit";
    private final String getStatus_Path = "/{name}/status";
    private final String getLog_Path = "/{name}/log";
    private final String getInfo_Path = "/info/{name}";
    private final String createBranch_Path = "/{name}/branch";
    private final String createCheckout_Path = "/{name}/checkout";
    private final String createMerge_Path = "/{name}/merge";
    private final String downloadFiles_Path = "/{name}/download";
    private final String getRepositories_Path = "/repositories";

    @PostConstruct
    public void init() {
        File reposDir = new File(baseGitRepo);
        if (!reposDir.exists()) {
            reposDir.mkdirs();
            log.info("Created repos directory: {}", reposDir.getAbsolutePath());
        } else {
            log.info("Repos directory already exists: {}", reposDir.getAbsolutePath());
            File[] repoDirs = reposDir.listFiles(File::isDirectory);
            if (repoDirs != null) {
                for (File repoDir : repoDirs) {
                    if (new File(repoDir, ".git").exists()) {
                        log.info("Found existing repository: {}", repoDir.getName());
                    }
                }
            }
        }
    }

    @GetMapping(getRepositories_Path)
    public ResponseEntity<List<String>> getRepositories() {
        try {
            File reposDir = new File(baseGitRepo);
            List<String> repoNames = new ArrayList<>();
            File[] repoDirs = reposDir.listFiles(File::isDirectory);
            if (repoDirs != null) {
                for (File repoDir : repoDirs) {
                    if (new File(repoDir, ".git").exists()) {
                        String repoName = repoDir.getName().replace("project_", "");
                        repoNames.add(repoName);
                    }
                }
            }
            log.info("Fetched repositories: {}", repoNames);
            return ResponseEntity.ok(repoNames);
        } catch (Exception e) {
            log.error("Error fetching repositories: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Collections.singletonList("Ошибка при получении списка репозиториев: " + e.getMessage()));
        }
    }

    @PostMapping(createRepository_Path)
    public ResponseEntity<String> createRepository(@PathVariable String name) {
        if (name == null || name.trim().isEmpty() || name.equals("undefined")) {
            log.error("Invalid project name: {}", name);
            return ResponseEntity.badRequest().body("Некорректное имя проекта");
        }
        String repoPath = getGitRepoPath(name);
        File repoDir = new File(repoPath);
        File gitDir = new File(repoDir, ".git");

        if (gitDir.exists()) {
            log.info("Repository already exists for project: {}", name);
            return ResponseEntity.ok("Репозиторий уже существует: " + repoPath);
        }

        try {
            log.info("Creating repository for project: {}", name);
            Git git = Git.init().setDirectory(repoDir).call();

            File readmeFile = new File(repoDir, "README.md");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(readmeFile))) {
                writer.write("# " + name + "\n");
                writer.write("Этот репозиторий был создан автоматически.\n");
            }

            git.add().addFilepattern("README.md").call();
            git.commit().setMessage("Первый коммит").call();

            log.info("Repository created successfully: {}", repoPath);
            return ResponseEntity.ok("Репозиторий создан: " + repoPath + ". Первый коммит выполнен.");
        } catch (GitAPIException e) {
            log.error("Error creating repository: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Ошибка при создании репозитория: " + e.getMessage());
        } catch (IOException e) {
            log.error("Error creating README file: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Ошибка при создании README файла: " + e.getMessage());
        }
    }

    @PostMapping(addFileToRepository_Path)
    public ResponseEntity<String> addFileToRepository(@PathVariable String name, @RequestParam("file") MultipartFile file) {
        if (name == null || name.trim().isEmpty() || name.equals("undefined")) {
            log.error("Invalid project name: {}", name);
            return ResponseEntity.badRequest().body("Некорректное имя проекта");
        }
        String repoPath = getGitRepoPath(name);
        try {
            File repoDir = getRepoDir(name);
            File localFile = new File(repoDir, file.getOriginalFilename());
            file.transferTo(localFile);

            Git git = Git.open(repoDir);
            git.add().addFilepattern(localFile.getName()).call();

            log.info("File added to repository: {}", localFile.getName());
            return ResponseEntity.ok("Файл добавлен в репозиторий: " + repoPath);
        } catch (Exception e) {
            log.error("Error adding file: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Ошибка при добавлении файла: " + e.getMessage());
        }
    }

    @PostMapping(value = commitToRepository_Path, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> commitToRepository(
            @PathVariable String name,
            @RequestPart("files") List<MultipartFile> files,
            @RequestPart("commitRequest") CommitRequest commitRequest) {
        if (name == null || name.trim().isEmpty() || name.equals("undefined")) {
            log.error("Invalid project name: {}", name);
            return ResponseEntity.badRequest().body("Некорректное имя проекта");
        }
        String repoPath = getGitRepoPath(name);
        try {
            File repoDir = getRepoDir(name);
            Git git = Git.open(repoDir);

            if (commitRequest.getBranch() != null) {
                git.checkout().setName(commitRequest.getBranch()).call();
            }

            for (MultipartFile file : files) {
                File tempFile = new File(repoDir, file.getOriginalFilename());
                file.transferTo(tempFile);
                git.add().addFilepattern(tempFile.getName()).call();
            }

            git.commit().setMessage(commitRequest.getMessage()).call();
            log.info("Commit added to branch: {}", commitRequest.getBranch());
            return ResponseEntity.ok("Коммит добавлен в ветку " + commitRequest.getBranch() + ".");
        } catch (IOException | GitAPIException e) {
            log.error("Error committing changes: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Ошибка при добавлении коммита: " + e.getMessage());
        }
    }

    @GetMapping(getStatus_Path)
    public ResponseEntity<String> getStatus(@PathVariable String name) {
        if (name == null || name.trim().isEmpty() || name.equals("undefined")) {
            log.error("Invalid project name: {}", name);
            return ResponseEntity.badRequest().body("Некорректное имя проекта");
        }
        String repoPath = getGitRepoPath(name);
        try {
            File repoDir = getRepoDir(name);
            Git git = Git.open(repoDir);
            String status = git.status().call().toString();
            log.info("Fetched status for repository {}: {}", name, status);
            return ResponseEntity.ok(status);
        } catch (IOException | GitAPIException e) {
            log.error("Error getting status: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Ошибка при получении статуса: " + e.getMessage());
        }
    }

    @GetMapping(getLog_Path)
    public ResponseEntity<String> getLog(@PathVariable String name) {
        if (name == null || name.trim().isEmpty() || name.equals("undefined")) {
            log.error("Invalid project name: {}", name);
            return ResponseEntity.badRequest().body("Некорректное имя проекта");
        }
        String repoPath = getGitRepoPath(name);
        try {
            File repoDir = getRepoDir(name);
            Git git = Git.open(repoDir);
            StringBuilder logBuilder = new StringBuilder();
            git.log().call().forEach(commit -> logBuilder.append(commit.getFullMessage()).append("\n"));
            log.info("Fetched logs for repository: {}", name);
            return ResponseEntity.ok(logBuilder.toString());
        } catch (IOException | GitAPIException e) {
            log.error("Error getting log: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Ошибка при получении логов: " + e.getMessage());
        }
    }

    @GetMapping(getInfo_Path)
    public ResponseEntity<RepositoryInfo> getInfo(@PathVariable String name) {
        if (name == null || name.trim().isEmpty() || name.equals("undefined")) {
            log.error("Invalid project name: {}", name);
            return ResponseEntity.badRequest().body(null);
        }
        String repoPath = getGitRepoPath(name);
        try {
            File repoDir = new File(repoPath);
            File gitDir = new File(repoDir, ".git");
            RepositoryInfo info = new RepositoryInfo();
            info.setName(name);
            info.setPath(repoPath);
            info.setExists(gitDir.exists());

            if (gitDir.exists()) {
                Git git = Git.open(repoDir);
                info.setCurrentBranch(git.getRepository().getBranch());
                List<String> branches = new ArrayList<>();
                git.branchList().call().forEach(ref -> branches.add(ref.getName().replace("refs/heads/", "")));
                info.setBranches(branches);
                git.close();
            }

            log.info("Repository info for {}: exists={}, currentBranch={}, branches={}",
                    name, info.isExists(), info.getCurrentBranch(), info.getBranches());
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            log.error("Error getting repository info for {}: {}", name, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PostMapping(createBranch_Path)
    public ResponseEntity<String> createBranch(@PathVariable String name, @RequestBody BranchRequest branchRequest) {
        if (name == null || name.trim().isEmpty() || name.equals("undefined")) {
            log.error("Invalid project name: {}", name);
            return ResponseEntity.badRequest().body("Некорректное имя проекта");
        }
        String repoPath = getGitRepoPath(name);
        try {
            File repoDir = getRepoDir(name);
            Git git = Git.open(repoDir);
            git.branchCreate().setName(branchRequest.getBranchName()).call();
            log.info("Created branch {} for repository {}", branchRequest.getBranchName(), name);
            return ResponseEntity.ok("Создана ветка " + branchRequest.getBranchName() + ".");
        } catch (Exception e) {
            log.error("Error creating branch: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Ошибка при создании ветки: " + e.getMessage());
        }
    }

    @PostMapping(createCheckout_Path)
    public ResponseEntity<String> createCheckout(@PathVariable String name, @RequestBody BranchRequest branchRequest) {
        if (name == null || name.trim().isEmpty() || name.equals("undefined")) {
            log.error("Invalid project name: {}", name);
            return ResponseEntity.badRequest().body("Некорректное имя проекта");
        }
        String repoPath = getGitRepoPath(name);
        try {
            File repoDir = getRepoDir(name);
            Git git = Git.open(repoDir);
            git.checkout().setName(branchRequest.getBranchName()).call();
            log.info("Checked out branch {} for repository {}", branchRequest.getBranchName(), name);
            return ResponseEntity.ok("Переключено на ветку " + branchRequest.getBranchName() + ".");
        } catch (Exception e) {
            log.error("Error checking out branch: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Ошибка при переключении ветки: " + e.getMessage());
        }
    }

    @PostMapping(createMerge_Path)
    public ResponseEntity<String> createMerge(@PathVariable String name, @RequestBody MergeRequest mergeRequest) {
        if (name == null || name.trim().isEmpty() || name.equals("undefined")) {
            log.error("Invalid project name: {}", name);
            return ResponseEntity.badRequest().body("Некорректное имя проекта");
        }
        String repoPath = getGitRepoPath(name);
        try {
            File repoDir = getRepoDir(name);
            Git git = Git.open(repoDir);
            git.merge().include(git.getRepository().findRef(mergeRequest.getBranchName())).call();
            log.info("Merged branch {} into current branch for repository {}", mergeRequest.getBranchName(), name);
            return ResponseEntity.ok("Ветка " + mergeRequest.getBranchName() + " слита в текущую ветку.");
        } catch (Exception e) {
            log.error("Error merging branch: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Ошибка при слиянии ветки: " + e.getMessage());
        }
    }

    @GetMapping(downloadFiles_Path)
    public ResponseEntity<byte[]> downloadFiles(@PathVariable String name, @RequestParam("branch") String branch) {
        if (name == null || name.trim().isEmpty() || name.equals("undefined")) {
            log.error("Invalid project name: {}", name);
            return ResponseEntity.badRequest().body("Некорректное имя проекта".getBytes());
        }
        String repoPath = getGitRepoPath(name);
        try {
            File repoDir = getRepoDir(name);
            Git git = Git.open(repoDir);

            git.checkout().setName(branch).call();

            File tempZip = File.createTempFile("repo_" + name, ".zip");
            try (FileOutputStream fos = new FileOutputStream(tempZip);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {
                addFilesToZip(repoDir, repoDir, zos);
            }

            byte[] zipBytes = Files.readAllBytes(tempZip.toPath());
            tempZip.delete();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", name + "_" + branch + ".zip");
            headers.setContentLength(zipBytes.length);

            log.info("Files downloaded for repository {} branch {}", name, branch);
            return new ResponseEntity<>(zipBytes, headers, HttpStatus.OK);
        } catch (IOException | GitAPIException e) {
            log.error("Error downloading files: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(("Ошибка при скачивании файлов: " + e.getMessage()).getBytes());
        }
    }

    private void addFilesToZip(File rootDir, File currentDir, ZipOutputStream zos) throws IOException {
        File[] files = currentDir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.getName().equals(".git")) continue;

            String relativePath = rootDir.toURI().relativize(file.toURI()).getPath();
            if (file.isDirectory()) {
                zos.putNextEntry(new ZipEntry(relativePath + "/"));
                zos.closeEntry();
                addFilesToZip(rootDir, file, zos);
            } else {
                zos.putNextEntry(new ZipEntry(relativePath));
                Files.copy(file.toPath(), zos);
                zos.closeEntry();
            }
        }
    }

    public String getGitRepoPath(String projectName) {
        return baseGitRepo + "project_" + projectName;
    }

    private File getRepoDir(String name) {
        File repoDir = new File(getGitRepoPath(name));
        if (!repoDir.exists() || !new File(repoDir, ".git").exists()) {
            log.warn("Repository does not exist: {}", repoDir.getAbsolutePath());
            throw new IllegalStateException("Репозиторий не существует: " + repoDir.getAbsolutePath());
        }
        return repoDir;
    }

    @GetMapping("/branches/{name}")
    public ResponseEntity<List<String>> fetchBranches(@PathVariable String name) {
        if (name == null || name.trim().isEmpty() || name.equals("undefined")) {
            log.error("Invalid project name: {}", name);
            return ResponseEntity.badRequest().body(Collections.singletonList("Некорректное имя проекта"));
        }
        String repoPath = getGitRepoPath(name);
        try {
            File repoDir = new File(repoPath);
            if (!repoDir.exists() || !new File(repoDir, ".git").exists()) {
                log.info("Repository not found for project: {}", name);
                return ResponseEntity.ok(Collections.emptyList());
            }
            Git git = Git.open(repoDir);
            List<String> branches = new ArrayList<>();
            git.branchList().call().forEach(ref -> branches.add(ref.getName()));
            log.info("Fetched branches for repository {}: {}", name, branches);
            return ResponseEntity.ok(branches);
        } catch (IOException | GitAPIException e) {
            log.error("Error fetching branches: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Collections.singletonList("Ошибка при получении веток: " + e.getMessage()));
        }
    }

    @PostMapping("/upload/{projectName}")
    public ResponseEntity<List<String>> uploadFiles(@PathVariable String projectName, @RequestParam("files") MultipartFile[] files) {
        if (projectName == null || projectName.trim().isEmpty() || projectName.equals("undefined")) {
            log.error("Invalid project name: {}", projectName);
            return ResponseEntity.badRequest().body(Collections.singletonList("Некорректное имя проекта"));
        }
        List<String> filePaths = new ArrayList<>();
        String relativePath = "uploads/" + projectName;
        String uploadDir = new File(relativePath).getAbsolutePath();

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                log.error("Failed to create directory: {}", uploadDir);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Collections.singletonList("Ошибка при создании директории: " + uploadDir));
            }
        }

        try {
            for (MultipartFile file : files) {
                String filePath = uploadDir + File.separator + file.getOriginalFilename();
                File dest = new File(filePath);
                file.transferTo(dest);
                filePaths.add(filePath);
            }
            log.info("Files uploaded to {}: {}", projectName, filePaths);
            return ResponseEntity.ok(filePaths);
        } catch (IOException e) {
            log.error("Error uploading files: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList("Ошибка при загрузке файлов: " + e.getMessage()));
        }
    }

    public class RepositoryInfo {
        private String name;
        private String path;
        private boolean exists;
        private String currentBranch;
        private List<String> branches = new ArrayList<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean isExists() {
            return exists;
        }

        public void setExists(boolean exists) {
            this.exists = exists;
        }

        public String getCurrentBranch() {
            return currentBranch;
        }

        public void setCurrentBranch(String currentBranch) {
            this.currentBranch = currentBranch;
        }

        public List<String> getBranches() {
            return branches;
        }

        public void setBranches(List<String> branches) {
            this.branches = branches;
        }
    }
}