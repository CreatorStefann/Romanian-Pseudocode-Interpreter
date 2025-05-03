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

    private Stmt whileStatement(){
        consume(PARANTEZA_STANGA, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(PARANTEZA_DREAPTA, "Expect ')' after condition.");
        Stmt body = statement();

        return new Stmt.While(condition, body);
    }
}
