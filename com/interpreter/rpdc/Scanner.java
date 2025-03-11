package com.interpreter.rpdc;

import java.util.*;

import static com.interpreter.rpdc.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 0;
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", SI);
        keywords.put("class", CLASA);
        keywords.put("else", ALTFEL);
        keywords.put("false", FALS);
        keywords.put("for", PENTRU);
        keywords.put("fun", FUNCTIE);
        keywords.put("if", DACA);
        keywords.put("nil", NIMIC);
        keywords.put("or", SAU);
        keywords.put("print", AFISEAZA);
        keywords.put("return", RETURNARE);
        keywords.put("super", SUPER);
        keywords.put("this", ACESTA);
        keywords.put("true", ADEVARAT);
        keywords.put("var", VARIABILA);
        keywords.put("while", CAT_TIMP);
    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while(!isAtEnd()){
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(':
                addToken(PARANTEZA_STANGA);
                break;
            case ')':
                addToken(PARANTEZA_DREAPTA);
                break;
            case '{':
                addToken(ACOLADA_STANGA);
                break;
            case '}':
                addToken(ACOLADA_DREAPTA);
                break;
            case ',':
                addToken(VIRGULA);
                break;
            case '.':
                addToken(PUNCT);
                break;
            case '-':
                addToken(MINUS);
                break;
            case '+':
                addToken(PLUS);
                break;
            case ';':
                addToken(PUNCT_SI_VIRGULA);
                break;
            case '*':
                addToken(STAR);
                break;
            case '!':
                addToken(match('=') ? NEGARE_EGAL : NEGARE);
                break;
            case '=':
                addToken(match('=') ? EGAL_EGAL : EGAL);
                break;
            case '<':
                addToken(match('=') ? MAI_MIC_EGAL : MAI_MIC);
                break;
            case '>':
                addToken(match('=') ? MAI_MARE_EGAL : MAI_MARE);
                break;
            case '/':
                if(match('/')){
                    while(peek() != '\n' && !isAtEnd()) advance();
                } else{
                    addToken(SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                // ignore whitespacae
                break;
            case '\n':
                line++;
                break;
            case '"':
                string();
                break;
            default:
                if(isDigit(c)){
                    number();
                } else if (isAlpha(c)){
                    identifier();
                }
                else{
                    Main.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private void identifier() {
        while(isAlphaNumeric(peek()))   advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if(type == null)    type = IDENTIFICATOR;
        addToken(IDENTIFICATOR);
    }

    private void number() {
        while(isDigit(peek()))  advance();

        if (peek() == '.' && isDigit(peekNext())){
            advance();
            while(isDigit(peek()))  advance();
        }

        addToken(NUMAR, Double.parseDouble(source.substring(start, current)));
    }

    private void string() {
        while(peek() != '"' && !isAtEnd()){
            if(peek() == '\n')  line++;
            advance();
        }

        if(isAtEnd()){
            Main.error(line, "Unterminated string.");
            return;
        }

        advance();

        String value = source.substring(start + 1, current - 1);
        addToken(SIR, value);
    }

    private boolean match(char expected) {
        if(isAtEnd())   return false;
        if(source.charAt(current) != expected)  return false;

        current++;
        return true;
    }

    // peek () e functie pentru LOOKAHEAD

    private char peek() {
        if(isAtEnd())   return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if(current + 1 >= source.length())  return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        current++;
        return source.charAt(current - 1);
    }

    private void addToken(TokenType type){
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal){
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
