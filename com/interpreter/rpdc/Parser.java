package com.interpreter.rpdc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static com.interpreter.rpdc.TokenType.*;

public class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    private Expr expression(){
        return assignment();
    }

    private Stmt statement(){
        if(match(PENTRU))   return forStatement();
        if(match(DACA))     return ifStatement();
        if(match(SCRIE))    return printStatement();
        if(match(INTOARCE))    return returnStatement();
        if(match(CAT_TIMP))     return whileStatement();
        if(match(ACOLADA_STANGA))     return new Stmt.Block(block());
        return expressionStatement();
    }

    private Stmt forStatement(){
        consume(PARANTEZA_STANGA, "Expect '(' after 'for'.");

        Stmt initializer;
        if(match(PUNCT_SI_VIRGULA)){
            initializer = null;
        } else if (match(VARIABILA)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if (!check(PUNCT_SI_VIRGULA)){
            condition = expression();
        }
        consume(PUNCT_SI_VIRGULA, "Expect ';' after loop condition.");

        Expr increment = null;
        if(!check(PARANTEZA_DREAPTA)){
            increment = expression();
        }
        consume(PARANTEZA_DREAPTA, "Expect ')' after for clauses.");

        Stmt body = statement();

        if (increment != null) {
            body = new Stmt.Block(
                    Arrays.asList(
                            body,
                            new Stmt.Expression(increment)));
        }

        if(condition == null)   condition = new Expr.Literal(true);
        body = new Stmt.While(condition, body);

        if(initializer != null) {
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }

        return body;
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(PUNCT_SI_VIRGULA)) {
            value = expression();
        }
        consume(PUNCT_SI_VIRGULA, "Expect ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt ifStatement() {
        consume(PARANTEZA_STANGA, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(PARANTEZA_DREAPTA, "Expect ')' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if(match(ALTFEL)){
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt printStatement(){
        Expr value = expression();
        consume(PUNCT_SI_VIRGULA, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement(){
        Expr expr = expression();
        consume(PUNCT_SI_VIRGULA, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private Stmt.Function function(String kind){
        Token name = consume(IDENTIFICATOR, "Expect " + kind + " name.");
        consume(PARANTEZA_STANGA, "Expect '(' after " + kind + " name.");
        List<Token> parameters = new ArrayList<>();
        if (!check(PARANTEZA_DREAPTA)) {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), "Can't have more than 255 parameters.");
                }
                parameters.add(
                        consume(IDENTIFICATOR, "Expect parameter name."));
            } while (match(VIRGULA));
        }
        consume(PARANTEZA_DREAPTA, "Expect ')' after parameters.");

        consume(ACOLADA_STANGA, "Expect '{' before " + kind + " body.");
        List<Stmt> body = block();
        return new Stmt.Function(name, parameters, body);
    }

    private List<Stmt> block(){
        List<Stmt> statements = new ArrayList<>();

        while(!check(ACOLADA_DREAPTA) && !isAtEnd()){
            statements.add(declaration());
        }

        consume(ACOLADA_DREAPTA, "Expect '}' after block.");
        return statements;
    }

    private Expr assignment(){
        Expr expr = or();

        if(match(ATRIBUIRE)){
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable){
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            } else if (expr instanceof Expr.Get) {
                Expr.Get get = (Expr.Get)expr;
                return new Expr.Set(get.object, get.name, value);
            }

            error(equals, "Invalid assignment target.");
        }
        return expr;
    }

    private Expr or(){
        Expr expr = and();

        while(match(SAU)){
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and(){
        Expr expr = equality();

        while(match(SI)){
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr equality(){
        Expr expr = comparison();

        while(match(NEGARE_EGAL, EGAL_EGAL)){
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private boolean match(TokenType... types){
        for(TokenType type : types){
            if(check(type)){
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType type){
        if(isAtEnd())   return false;
        return peek().type == type;
    }

    private Token advance(){
        if(!isAtEnd())  current++;
        return previous();
    }

    private boolean isAtEnd(){
        return peek().type == EOF;
    }

    private Token peek(){
        return tokens.get(current);
    }

    private Token previous(){
        return tokens.get(current - 1);
    }

    private Expr comparison(){
        Expr expr = term();

        while(match(MAI_MARE, MAI_MARE_EGAL, MAI_MIC, MAI_MIC_EGAL)){
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term(){
        Expr expr = factor();

        while(match(MINUS, PLUS)){
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor(){
        Expr expr = unary();

        while(match(SLASH, STAR)){
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary(){
        if(match(NEGARE, MINUS)){
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return call();
    }

    private Expr finishCall(Expr callee){
        List<Expr> arguments = new ArrayList<>();
        if(!check(PARANTEZA_DREAPTA)){
            do{
                if(arguments.size() >= 255) {
                    error(peek(), "Can't have more than 255 arguments.");
                }
                arguments.add(expression());
            } while(match(VIRGULA));
        }

        Token paren = consume(PARANTEZA_DREAPTA, "Expect ')' after arguments.");

        return new Expr.Call(callee, paren, arguments);
    }

    private Expr call(){
        Expr expr = primary();

        while(true){
            if(match(PARANTEZA_STANGA)){
                expr = finishCall(expr);
            } else if (match(PUNCT)) {
                Token name = consume(IDENTIFICATOR,
                        "Expect property name after '.'.");
                expr = new Expr.Get(expr, name);
            } else {
                break;
            }
        }

        return expr;
    }

    private Expr primary(){
        if(match(FALS))     return new Expr.Literal(false);
        if(match(ADEVARAT))     return new Expr.Literal(true);
        if(match(NIMIC))     return new Expr.Literal(null);

        if(match(NUMAR, SIR)){
            return new Expr.Literal(previous().literal);
        }

        if (match(SUPER)) {
            Token keyword = previous();
            consume(PUNCT, "Expect '.' after 'super'.");
            Token method = consume(IDENTIFICATOR,
                    "Expect superclass method name.");
            return new Expr.Super(keyword, method);
        }

        if (match(ACESTA)) return new Expr.This(previous());

        if(match(IDENTIFICATOR)){
            return new Expr.Variable(previous());
        }

        if(match(PARANTEZA_STANGA)){
            Expr expr = expression();
            consume(PARANTEZA_DREAPTA, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    private Token consume(TokenType type, String message){
        if(check(type))     return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message){
        Main.error(token, message);
        return new ParseError();
    }

    private void syncronize(){
        advance();

        while(!isAtEnd()){
            if(previous().type == PUNCT_SI_VIRGULA) return;

            switch(peek().type){
                case CLASA:
                case FUNCTIE:
                case VARIABILA:
                case PENTRU:
                case DACA:
                case CAT_TIMP:
                case SCRIE:
                case INTOARCE:
                    return;
            }

            advance();
        }
    }

    List<Stmt> parse(){
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()){
            statements.add(declaration());
        }

        return statements;
    }

    private Stmt declaration(){
        try{
            if(match(CLASA))    return classDeclaration();
            if(match(FUNCTIE))  return function("function");
            if(match(VARIABILA))    return varDeclaration();
            return statement();
        }   catch(ParseError error){
            syncronize();
            return null;
        }
    }

    private Stmt classDeclaration() {
        Token name = consume(IDENTIFICATOR, "Expect class name.");

        Expr.Variable superclass = null;
        if (match(MAI_MIC)) {
            consume(IDENTIFICATOR, "Expect superclass name.");
            superclass = new Expr.Variable(previous());
        }

        consume(ACOLADA_STANGA, "Expect '{' before class body.");
        List<Stmt.Function> methods = new ArrayList<>();
        while (!check(ACOLADA_DREAPTA) && !isAtEnd()) {
            methods.add(function("method"));
        }
        consume(ACOLADA_DREAPTA, "Expect '}' after class body.");

        return new Stmt.Class(name, superclass, methods);
    }

    private Stmt varDeclaration(){
        Token name = consume(IDENTIFICATOR, "Expect variable name.");

        Expr initializer = null;
        if (match(ATRIBUIRE)){
            initializer = expression();
        }

        consume(PUNCT_SI_VIRGULA, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt whileStatement(){
        consume(PARANTEZA_STANGA, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(PARANTEZA_DREAPTA, "Expect ')' after condition.");
        Stmt body = statement();

        return new Stmt.While(condition, body);
    }
}
