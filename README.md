
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

### Hello World

```pseudo
// Primul program în pseudocod!
scrie "Salut, lume!"
```

### Variables and Assignment

```pseudo
variabila nume ← "Ana"
variabila varsta ← 25
variabila pret ← 12.5
```

### Arithmetic Operations

```pseudo
scrie 5 + 3 * 2 - 8 / 4  // 7
scrie -10                // Negare numerică
scrie "Salut " + "Lume!" // Concatenare șir de caractere
```

### Comparison Operators

```pseudo
scrie 1 = 2  // Fals
scrie 3 ≠ 4  // Adevărat
scrie 5 < 10 // Adevărat
scrie 20 ≥ 15 // Adevărat
```

### Logical Operators

```pseudo
scrie !adevarat  // Fals
scrie adevarat SI fals  // Fals
scrie adevarat SAU fals  // Adevărat
```

### If-Else Statement

```pseudo
daca a < b atunci
    scrie "A este mai mic decât B"
altfel
    scrie "B este mai mic sau egal cu A"
sfarsitdaca
```

### While Loop

```pseudo
x ← 0
cat timp x < 5 executa
    scrie x
    x ← x + 1
sfarsitcattimp
```

### For Loop

```pseudo
pentru i ← 0, 4, 1 executa
    scrie i
sfarsitpentru
```

### Functions

```pseudo
functie aduna(a, b)
    intoarce a + b
sfarsitfunctie

rezultat ← aduna(3, 4)
scrie rezultat  // 7
```

### Classes and Objects

```pseudo
clasa MicDejun
    procedura gateste()
        scrie "Ouale se prajesc!"
    sfarsitprocedura
sfarsitclasa

micDejun ← MicDejun()
micDejun.gateste()
```

## Roadmap
- [ ] Support for classes and objects
- [ ] Performance optimizations
- [x] Interactive REPL mode

## Credits
Inspired by *Crafting Interpreters* by Robert Nystrom.  