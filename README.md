# Romanian-Pseudocode-Interpreter

This is an interpreter for a pseudocode language in Romanian, inspired by *Crafting Interpreters* by Robert Nystrom. The implementation is done in Java, using a tree-walk interpreter.

## Features
- **Lexing & Parsing** – Tokenization and Abstract Syntax Tree (AST) construction.
- **Tree-Walk Interpretation** – Executes code directly from the AST.
- **Dynamic Typing** – Supports numbers, strings, booleans, and `nothing`.
- **Control Structures** – Implements `if`, `else`, `while`, and `for each`.
- **Functions & Variables** – Supports user-defined functions and global variables.
- **Error Handling** – Detects and displays clear messages for syntax and runtime errors.

## How to Use

### Requirements
- **Java 11+**
- **Terminal or IDE** (e.g., IntelliJ, VS Code)

### Running the Interpreter
Clone the repository and navigate to the project directory:

```sh
git clone https://github.com/CreatorStefann/Romanian-Pseudocode-Interpreter.git
cd Romanian-Pseudocode-Interpreter
```

Open this folder in your preferred IDE and run the main file ('Main').

## Example Code

```pseudo
scrie "Salut, lume!"
functie aduna(a, b) { returneaza a + b; }
scrie aduna(3, 4) // 7
```

## Roadmap
- [ ] Support for classes and objects
- [ ] Performance optimizations
- [ ] Interactive REPL mode

## Credits
Inspired by *Crafting Interpreters* by Robert Nystrom.  
