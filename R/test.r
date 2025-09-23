# Script de Teste para Instalação do R
# Este script verifica se a instalação do R está funcionando corretamente

cat("=== TESTE DE INSTALAÇÃO DO R ===\n\n")

# 1. Teste básico do R
cat("1. TESTE BÁSICO:\n")
cat("Versão do R:", R.version.string, "\n")
cat("Sistema operacional:", R.version$platform, "\n")
cat("Diretório de trabalho:", getwd(), "\n\n")

# 2. Teste de operações matemáticas básicas
cat("2. TESTE DE OPERAÇÕES MATEMÁTICAS:\n")
a <- 10
b <- 5
cat("10 + 5 =", a + b, "\n")
cat("10 * 5 =", a * b, "\n")
cat("10 / 5 =", a / b, "\n")
cat("sqrt(25) =", sqrt(25), "\n")
cat("log(100) =", log(100), "\n\n")

# 3. Teste de estruturas de dados
cat("3. TESTE DE ESTRUTURAS DE DADOS:\n")

# Vetor
vetor <- c(1, 2, 3, 4, 5)
cat("Vetor:", vetor, "\n")

# Matriz
matriz <- matrix(1:9, nrow = 3)
cat("Matriz 3x3:\n")
print(matriz)

# Data Frame
df <- data.frame(
  Nome = c("Ana", "João", "Maria"),
  Idade = c(25, 30, 35),
  Cidade = c("SP", "RJ", "BH")
)
cat("Data Frame:\n")
print(df)
cat("\n")

# 4. Teste de gráficos básicos
cat("4. TESTE DE GRÁFICOS:\n")
cat("Criando gráfico de teste...\n")

# Gráfico simples
tryCatch({
  png("teste_grafico.png", width = 600, height = 400)
  plot(1:10, 1:10, main = "Gráfico de Teste", 
       xlab = "Eixo X", ylab = "Eixo Y", 
       col = "blue", pch = 16)
  abline(h = 5, col = "red", lty = 2)
  legend("topleft", legend = "Pontos de teste", col = "blue", pch = 16)
  dev.off()
  cat("✓ Gráfico salvo como 'teste_grafico.png'\n")
}, error = function(e) {
  cat("✗ Erro ao criar gráfico:", e$message, "\n")
})

# 5. Teste de instalação de pacotes
cat("5. TESTE DE PACOTES:\n")

pacotes_necessarios <- c("dplyr", "ggplot2", "readr")

for (pacote in pacotes_necessarios) {
  if (requireNamespace(pacote, quietly = TRUE)) {
    cat("✓", pacote, "está instalado\n")
  } else {
    cat("✗", pacote, "NÃO está instalado\n")
  }
}

# 6. Teste de funções personalizadas
cat("6. TESTE DE FUNÇÕES:\n")

# Função simples
calcular_media <- function(x) {
  mean(x, na.rm = TRUE)
}

numeros <- c(1, 2, 3, 4, 5, NA)
cat("Média de", numeros, "=", calcular_media(numeros), "\n")

# 7. Teste de leitura/gravação de arquivos
cat("7. TESTE DE ARQUIVOS:\n")

# Criar e salvar arquivo CSV
tryCatch({
  write.csv(df, "teste_dataframe.csv", row.names = FALSE)
  cat("✓ Arquivo CSV criado: 'teste_dataframe.csv'\n")
  
  # Ler arquivo CSV
  df_lido <- read.csv("teste_dataframe.csv")
  cat("✓ Arquivo CSV lido com sucesso\n")
  cat("   Primeiras linhas:\n")
  print(head(df_lido))
}, error = function(e) {
  cat("✗ Erro com arquivos:", e$message, "\n")
})

# 8. Teste de performance básico
cat("8. TESTE DE PERFORMANCE:\n")

inicio <- Sys.time()

# Operação que demanda um pouco de processamento
resultado <- sum(rnorm(1000000))

fim <- Sys.time()
tempo <- fim - inicio

cat("Cálculo de 1.000.000 números aleatórios:\n")
cat("Resultado:", round(resultado, 2), "\n")
cat("Tempo de execução:", round(tempo, 3), "segundos\n")

# 9. Informações do sistema
cat("9. INFORMAÇÕES DO SISTEMA:\n")
cat("Memória disponível:\n")
print(pryr::mem_used())

cat("\n=== FIM DO TESTE ===\n")

# Limpeza
cat("\nArquivos criados durante o teste:\n")
cat("- teste_grafico.png\n")
cat("- teste_dataframe.csv\n")
cat("\nPara limpar, execute: file.remove('teste_grafico.png', 'teste_dataframe.csv')\n")

# Mensagem final
cat("\n🎉 Se todos os testes passaram sem erros, sua instalação do R está funcionando corretamente!\n")