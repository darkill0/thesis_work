<template>
  <div id="app" class="app-container">
    <div class="sidebar">
      <h2>Записи</h2>
      <div
          v-for="(note, index) in notes"
          :key="note.id"
          class="note"
          draggable="true"
          @dragstart="handleDragStart(note, index, $event)"
          :style="{ backgroundColor: note.color }"
      >
        {{ note.text }}
      </div>
      <button class="btn btn-primary" @click="showAddCubeModal">Добавить кубарик</button>
    </div>

    <div
        class="content"
        ref="contentArea"
        @drop="handleDrop"
        @dragover.prevent
        @wheel="handleWheel"
        @mousedown="startPanning"
        @mousemove="pan"
        @mouseup="stopPanning"
        @mouseleave="stopPanning"
        :style="{ cursor: isPanning ? 'grabbing' : 'grab' }"
    >
      <div
          class="canvas"
          :style="{
            transform: `scale(${zoomScale}) translate(${panX}px, ${panY}px)`,
            width: `${canvasWidth}px`,
            height: `${canvasHeight}px`
          }"
      >
        <div
            class="cube"
            v-for="cube in cubes"
            :key="cube.id"
            :id="'cube-' + cube.id"
            :style="{
              left: cube.x + 'px',
              top: cube.y + 'px',
              backgroundColor: cube.color,
              width: '150px',
              height: '150px'
            }"
            @mousedown.stop.prevent="startDrag(cube, $event)"
            @contextmenu.prevent="showContextMenu(cube, $event)"
        >
          {{ cube.text }}
        </div>
      </div>
    </div>

    <!-- Модальное окно -->
    <div class="modal fade" id="addCubeModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Создать новый кубарик</h5>
            <button type="button" class="btn-close" @click="closeModal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <input v-model="newNoteText" placeholder="Описание кубарика" class="form-control" />
            <input v-model="newNoteColor" type="color" class="form-control mt-2" />
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeModal">Закрыть</button>
            <button type="button" class="btn btn-primary" @click="addCube">Создать</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Контекстное меню -->
    <div
        v-if="contextMenuVisible"
        class="context-menu"
        :style="{ top: contextMenuY + 'px', left: contextMenuX + 'px' }"
    >
      <ul>
        <li @click="createConnectionPoint(selectedCube)"><i class="fas fa-link"></i> Создать связь</li>
        <li @click="removeConnection(selectedCube)"><i class="fas fa-trash"></i> Удалить связь</li>
        <li @click="removeCube(selectedCube)"><i class="fas fa-times"></i> Удалить куб</li>
      </ul>
    </div>
  </div>
</template>

<script>
import { jsPlumb } from 'jsplumb';
import { Modal } from 'bootstrap';
import apiClient from "@/utils/apiClient";

export default {
  name: "KanbanBoard",
  props: {
    type: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      notes: [],
      cubes: [],
      draggingCube: null,
      offsetX: 0,
      offsetY: 0,
      jsPlumbInstance: null,
      selectedCube: null,
      contextMenuVisible: false,
      contextMenuX: 0,
      contextMenuY: 0,
      newNoteText: '',
      newNoteColor: '#ffffff',
      connectionSource: null,
      userId: null,
      entityId: null,
      entityType: this.type,
      zoomScale: 1,
      isPanning: false,
      panX: 0,
      panY: 0,
      startPanX: 0,
      startPanY: 0,
      canvasWidth: 0,
      canvasHeight: 0,
      draggedCubes: new Set()
    };
  },
  mounted() {
    this.userId = this.$route.params.id;
    this.loadCanvasState();
    this.initializeJsPlumb();
    this.loadTodos();
    this.updateCanvasSize();
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
    document.removeEventListener('mousemove', this.dragCube);
    document.removeEventListener('mouseup', this.stopDrag);
    this.saveCanvasState();
  },
  methods: {
    initializeJsPlumb() {
      this.jsPlumbInstance = jsPlumb.getInstance({
        Container: this.$refs.contentArea.querySelector('.canvas'),
        Connector: ['Straight'],
        Endpoint: ['Dot', { radius: 5 }],
        Anchor: 'Continuous'
      });
      this.jsPlumbInstance.bind('connection', this.handleConnection);
    },

    updateCanvasSize() {
      const contentArea = this.$refs.contentArea;
      this.canvasWidth = contentArea.clientWidth / this.zoomScale;
      this.canvasHeight = contentArea.clientHeight / this.zoomScale;
      this.saveCanvasState();
    },

    loadCanvasState() {
      const savedState = localStorage.getItem(`kanbanState_${this.userId}_${this.entityType}`);
      if (savedState) {
        const { zoomScale, panX, panY } = JSON.parse(savedState);
        this.zoomScale = zoomScale || 1;
        this.panX = panX || 0;
        this.panY = panY || 0;
      }
    },

    saveCanvasState() {
      const state = {
        zoomScale: this.zoomScale,
        panX: this.panX,
        panY: this.panY
      };
      localStorage.setItem(`kanbanState_${this.userId}_${this.entityType}`, JSON.stringify(state));
    },

    loadTodos() {
      const endpoints = {
        task: `/todo/user/${this.userId}/task/${this.$route.params.taskId}`,
        project: `/todo/user/${this.userId}/project/${this.$route.params.projectId}`,
        ticket: `/todo/user/${this.userId}/ticket/${this.$route.params.ticketId}`,
        default: `/todo/user/${this.userId}`
      };
      const url = endpoints[this.entityType] || endpoints.default;

      apiClient.get(url).then(response => {
        this.processTodos(response.data);
      }).catch(error => console.error('Error loading todos:', error));
    },

    processTodos(data) {
      data.forEach(todo => {
        const item = {
          id: todo.id,
          text: todo.description,
          color: todo.color,
          status: todo.status,
          projectId: todo.projectId,
          taskId: todo.taskId,
          ticketId: todo.ticketId
        };

        if (todo.x && todo.y) {
          item.x = todo.x;
          item.y = todo.y;
          this.cubes.push(item);
          this.$nextTick(() => this.makeDraggable(item));
          if (todo.connectedTodoIds?.length) {
            this.$nextTick(() => {
              todo.connectedTodoIds.forEach(targetId => {
                this.jsPlumbInstance.connect({
                  source: `cube-${item.id}`,
                  target: `cube-${targetId}`
                });
              });
            });
          }
        } else {
          this.notes.push(item);
        }
      });
    },

    handleWheel(event) {
      if (event.ctrlKey) {
        event.preventDefault();
        const oldScale = this.zoomScale;
        const delta = event.deltaY > 0 ? -0.1 : 0.1;
        this.zoomScale = Math.max(0.2, Math.min(2, this.zoomScale + delta));

        const rect = this.$refs.contentArea.getBoundingClientRect();
        const mouseX = (event.clientX - rect.left) / oldScale;
        const mouseY = (event.clientY - rect.top) / oldScale;
        this.panX = (this.panX + mouseX) * (this.zoomScale / oldScale) - mouseX;
        this.panY = (this.panY + mouseY) * (this.zoomScale / oldScale) - mouseY;

        this.updateCanvasSize();
      }
    },

    startPanning(event) {
      // Панорамирование запускается только если клик был вне куба
      if (event.button === 0 && !event.target.closest('.cube')) {
        this.isPanning = true;
        this.startPanX = event.clientX;
        this.startPanY = event.clientY;
      }
    },

    pan(event) {
      if (this.isPanning) {
        const dx = (event.clientX - this.startPanX) / this.zoomScale;
        const dy = (event.clientY - this.startPanY) / this.zoomScale;
        this.panX += dx;
        this.panY += dy;
        this.startPanX = event.clientX;
        this.startPanY = event.clientY;
        this.jsPlumbInstance.repaintEverything();
        this.saveCanvasState();
      }
    },

    stopPanning() {
      this.isPanning = false;
    },

    getConnectedCubes(cube) {
      const connectedCubes = new Set();
      const stack = [cube];

      while (stack.length > 0) {
        const currentCube = stack.pop();
        if (!connectedCubes.has(currentCube)) {
          connectedCubes.add(currentCube);
          const connections = this.jsPlumbInstance.getConnections({ source: `cube-${currentCube.id}` })
              .concat(this.jsPlumbInstance.getConnections({ target: `cube-${currentCube.id}` }));

          connections.forEach(conn => {
            const targetId = conn.targetId === `cube-${currentCube.id}` ? conn.sourceId : conn.targetId;
            const targetCube = this.cubes.find(c => `cube-${c.id}` === targetId);
            if (targetCube && !connectedCubes.has(targetCube)) {
              stack.push(targetCube);
            }
          });
        }
      }

      return connectedCubes;
    },

    startDrag(cube, event) {
      this.draggingCube = cube;
      this.draggedCubes = this.getConnectedCubes(cube);
      const rect = this.$refs.contentArea.getBoundingClientRect();
      this.offsetX = (event.clientX - rect.left) / this.zoomScale - cube.x;
      this.offsetY = (event.clientY - rect.top) / this.zoomScale - cube.y;
      document.addEventListener('mousemove', this.dragCube);
      document.addEventListener('mouseup', this.stopDrag);
    },

    dragCube(event) {
      if (this.draggingCube) {
        const rect = this.$refs.contentArea.getBoundingClientRect();
        const newX = (event.clientX - rect.left) / this.zoomScale - this.offsetX;
        const newY = (event.clientY - rect.top) / this.zoomScale - this.offsetY;
        const dx = newX - this.draggingCube.x;
        const dy = newY - this.draggingCube.y;

        this.draggedCubes.forEach(cube => {
          cube.x += dx;
          cube.y += dy;
        });

        this.draggingCube.x = newX;
        this.draggingCube.y = newY;
        this.jsPlumbInstance.repaintEverything();
      }
    },

    stopDrag() {
      if (this.draggingCube) {
        const updates = [];
        this.draggedCubes.forEach(cube => {
          updates.push(apiClient.post(`/todo/todo/${cube.id}`, {
            x: cube.x,
            y: cube.y
          }).catch(error => console.error(`Error updating position for cube ${cube.id}:`, error)));
        });
        Promise.all(updates).then(() => console.log('All positions updated'));
      }
      this.draggingCube = null;
      this.draggedCubes.clear();
      document.removeEventListener('mousemove', this.dragCube);
      document.removeEventListener('mouseup', this.stopDrag);
    },

    handleDragStart(note, index, event) {
      event.dataTransfer.setData('text/plain', JSON.stringify({ note, index }));
    },

    handleDrop(event) {
      event.preventDefault();
      const { note, index } = JSON.parse(event.dataTransfer.getData('text/plain'));
      const rect = this.$refs.contentArea.getBoundingClientRect();
      const newCube = {
        id: note.id,
        text: note.text,
        color: note.color,
        x: (event.clientX - rect.left) / this.zoomScale - this.panX,
        y: (event.clientY - rect.top) / this.zoomScale - this.panY
      };

      this.cubes.push(newCube);
      this.notes.splice(index, 1);

      apiClient.post(`/todo/todo/${note.id}`, { x: newCube.x, y: newCube.y })
          .catch(error => console.error('Error updating position:', error));

      this.$nextTick(() => this.makeDraggable(newCube));
    },

    makeDraggable(cube) {
      this.jsPlumbInstance.draggable(`cube-${cube.id}`);
      this.jsPlumbInstance.makeSource(`cube-${cube.id}`, { anchor: 'Continuous' });
      this.jsPlumbInstance.makeTarget(`cube-${cube.id}`, { anchor: 'Continuous' });
    },

    showAddCubeModal() {
      const modal = new Modal(document.getElementById('addCubeModal'));
      modal.show();
    },

    closeModal() {
      const modal = Modal.getInstance(document.getElementById('addCubeModal'));
      modal.hide();
    },

    async addCube() {
      if (!this.newNoteText.trim()) return;

      const newNote = {
        description: this.newNoteText,
        color: this.newNoteColor,
        projectId: this.entityType === 'project' ? this.entityId : null,
        taskId: this.entityType === 'task' ? this.entityId : null,
        ticketId: this.entityType === 'ticket' ? this.entityId : null
      };

      try {
        const response = await apiClient.post(`/todo/user/${this.userId}/create/`, newNote);
        this.notes.push({
          id: response.data.id,
          text: response.data.description,
          color: response.data.color,
          status: response.data.status
        });
        this.newNoteText = '';
        this.newNoteColor = '#ffffff';
        this.closeModal();
      } catch (error) {
        console.error('Error creating note:', error);
      }
    },

    showContextMenu(cube, event) {
      this.selectedCube = cube;
      this.contextMenuX = event.clientX;
      this.contextMenuY = event.clientY;
      this.contextMenuVisible = true;
      document.addEventListener('click', this.hideContextMenu);
    },

    hideContextMenu() {
      this.contextMenuVisible = false;
      document.removeEventListener('click', this.hideContextMenu);
    },

    createConnectionPoint(cube) {
      if (!this.connectionSource) {
        this.connectionSource = cube;
      } else if (this.connectionSource.id !== cube.id) {
        this.jsPlumbInstance.connect({
          source: `cube-${this.connectionSource.id}`,
          target: `cube-${cube.id}` // Исправлено: cube вместо connectionSource
        });
        this.connectionSource = null;
      }
      this.hideContextMenu();
    },

    handleConnection(info) {
      const connectionData = {
        sourceId: info.sourceId.split('-')[1],
        targetId: info.targetId.split('-')[1]
      };
      apiClient.post('/todo/connections', connectionData)
          .catch(error => console.error('Error saving connection:', error));
    },

    removeConnection(cube) {
      this.jsPlumbInstance.getConnections({ source: `cube-${cube.id}` })
          .forEach(conn => this.jsPlumbInstance.deleteConnection(conn));
      this.hideContextMenu();
    },

    removeCube(cube) {
      this.jsPlumbInstance.remove(`cube-${cube.id}`);
      this.cubes = this.cubes.filter(c => c.id !== cube.id);
      this.hideContextMenu();
    },

    handleResize() {
      this.updateCanvasSize();
      this.jsPlumbInstance.repaintEverything();
    }
  }
};
</script>

<style scoped>
.app-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  width: 300px;
  padding: 20px;
  background-color: #f8f9fa;
  border-right: 1px solid #dee2e6;
  overflow-y: auto;
  flex-shrink: 0;
  z-index: 10;
}

.note {
  margin: 10px 0;
  padding: 10px;
  border-radius: 5px;
  cursor: grab;
  user-select: none;
}

.note:hover {
  opacity: 0.9;
}

.content {
  flex: 1;
  position: relative;
  overflow: hidden;
  background-color: #f0f0f0;
}

.canvas {
  position: absolute;
  top: 0;
  left: 0;
  transform-origin: 0 0;
  user-select: none;
}

.cube {
  position: absolute;
  color: white;
  text-align: center;
  border-radius: 5px;
  cursor: move;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px;
  z-index: 5;
}

.context-menu {
  position: fixed;
  background-color: white;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  z-index: 1000;
}

.context-menu ul {
  list-style: none;
  padding: 5px 0;
  margin: 0;
}

.context-menu ul li {
  padding: 8px 15px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
}

.context-menu ul li:hover {
  background-color: #f1f1f1;
}

.btn-primary {
  width: 100%;
  margin-top: 10px;
}
</style>