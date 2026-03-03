# Salão Financeiro Native

Projeto Android nativo em **Kotlin + Jetpack Compose + Room**, criado como evolução da UI da v3.1 híbrida.#ver rep https://github.com/ruffghanor20/Controlador-financeiro.git

## O que este pacote entrega
  - mesma linguagem visual da UI anterior (dark, cards, roxo primário, drawer lateral)
  - **Compose nativo** (sem WebView)
  - **Room** como banco local
  - **splash screen nativa**
  - login por **usuário/senha** e **PIN**
  - módulos:
  - Dashboard
  - Serviços
  - Atendimentos
  - Despesas
  - Produtos
  - Colaboradores
  - Relatório
  - Importar
  - Configurações
  - **backup JSON** em Downloads
  - **exportação CSV mensal** em Downloads
  - upload de **logo** e **anexos** por seletor nativo (URI persistida)

## Stack e versões usadas
- AGP: **8.12.1**
- Kotlin: **2.1.10**
- Compose BOM: **2026.02.01**
- Activity Compose: **1.12.4**
- Lifecycle: **2.10.0**
- Room: **2.8.4**

## Abrir no Android Studio
1. Abra a pasta deste projeto no Android Studio
2. Aguarde o Sync Gradle
3. Rode em um emulador ou aparelho físico

## Credenciais padrão
- usuário: `admin`
- senha: `admin123`

O PIN começa desativado. Você define em **Configurações**.

## Observações
  - O projeto foi montado para ficar **pronto para evolução**, não como produto final fechado.
  - O fluxo de anexos usa **URI persistida** do Android (mais robusto que base64).
  - A UI foi portada para Compose, mas ainda pode ser refinada com:
  - DatePicker nativo
  - filtros avançados
  - biometria
  - gráficos animados
  - importação de Excel
