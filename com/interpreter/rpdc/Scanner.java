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
        keywords.put("si", SI);
        keywords.put("clasa", CLASA);
        keywords.put("altfel", ALTFEL);
        keywords.put("fals", FALS);
        keywords.put("pentru", PENTRU);
        keywords.put("functie", FUNCTIE);
        keywords.put("daca", DACA);
        keywords.put("nimic", NIMIC);
        keywords.put("sau", SAU);
        keywords.put("scrie", SCRIE);
        keywords.put("intoarce", RETURNARE);
        keywords.put("super", SUPER);
        keywords.put("acesta", ACESTA);
        keywords.put("adevarat", ADEVARAT);
        keywords.put("variabila", VARIABILA);
        keywords.put("cattimp", CAT_TIMP);
        keywords.put("procedura", PROCEDURA);
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
                addToken(EGAL_EGAL);
                break;
            case '<':
                if (match('-')) {
                    addToken(ATRIBUIRE); // Detectează `<-` ca atribuire
                }else if (match('=')) {
                    addToken(MAI_MIC_EGAL);
                }
                else {
                    addToken(MAI_MIC);
                }
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
        addToken(type);
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
