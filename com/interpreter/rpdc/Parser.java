package com.interpreter.rpdc;

import java.util.ArrayList;
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
        return equality();
    }

    private Stmt statement(){
        if(match(SCRIE))    return printStatement();
        return expressionStatement();
    }

    private Stmt printStatement(){
        Expr value = expression();
        consume(PUNCT_SI_VIRGULA, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement(){
        Expr expr = expression();
        consume(PUNCT_SI_VIRGULA, "Expect ';' after expression.");
        return new Stmt.Print(expr);
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

        return primary();
    }

    private Expr primary(){
        if(match(FALS))     return new Expr.Literal(false);
        if(match(ADEVARAT))     return new Expr.Literal(true);
        if(match(NIMIC))     return new Expr.Literal(null);

        if(match(NUMAR, SIR)){
            return new Expr.Literal(previous().literal);
        }

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
                case RETURNARE:
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
            if(match(VARIABILA))    return varDeclaration();

            return statement();
        }   catch(ParseError error){
            syncronize();
            return null;
        }
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
}
