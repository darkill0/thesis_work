package org.example.restapi.api.git;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommitRequest {
    private String message;
    private String branch;
    private List<MultipartFile> files = new ArrayList<>();

}
