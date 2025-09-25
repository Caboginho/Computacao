# Projeto: Jogo da Velha OpenGL
# Compilar com: make (Linux) ou mingw32-make (Windows com MinGW)

# Nome do executável
TARGET = jogovelha

# Diretórios
SRC_DIR = src
INC_DIR = include
ASSETS_DIR = assets
OBJ_DIR = build

# Fontes
SRCS = $(wildcard $(SRC_DIR)/*.cpp) main.cpp
OBJS = $(patsubst %.cpp,$(OBJ_DIR)/%.o,$(notdir $(SRCS)))

# Compilador
CXX = g++
CXXFLAGS = -I$(INC_DIR) -Wall -O2

# Bibliotecas para Windows (MinGW)
LIBS_WIN = -lopengl32 -lglu32 -lfreeglut -lwinmm

# Bibliotecas para Linux
LIBS_LINUX = -lGL -lGLU -lglut

# Detectar SO
ifeq ($(OS),Windows_NT)
    LIBS = $(LIBS_WIN)
    RM = del /Q
    EXE = .exe
else
    LIBS = $(LIBS_LINUX)
    RM = rm -f
    EXE =
endif

# Regras
all: $(TARGET)$(EXE)

$(TARGET)$(EXE): $(OBJS)
	$(CXX) -o $@ $^ $(LIBS)

$(OBJ_DIR)/%.o: $(SRC_DIR)/%.cpp | $(OBJ_DIR)
	$(CXX) $(CXXFLAGS) -c $< -o $@

$(OBJ_DIR)/%.o: %.cpp | $(OBJ_DIR)
	$(CXX) $(CXXFLAGS) -c $< -o $@

$(OBJ_DIR):
	mkdir -p $(OBJ_DIR)

clean:
	$(RM) $(OBJ_DIR)/*.o $(TARGET)$(EXE)

run: all
	./$(TARGET)$(EXE)
