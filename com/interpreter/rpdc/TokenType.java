package com.interpreter.rpdc;

enum TokenType {
    // Single-character tokens
    PARANTEZA_STANGA, PARANTEZA_DREAPTA, ACOLADA_STANGA, ACOLADA_DREAPTA,
    VIRGULA, PUNCT, MINUS, PLUS, PUNCT_SI_VIRGULA, SLASH, STAR,

    // One or two character tokens
    NEGARE, NEGARE_EGAL,
    ATRIBUIRE, EGAL_EGAL,
    MAI_MARE, MAI_MARE_EGAL,
    MAI_MIC, MAI_MIC_EGAL,

    // Literals
    IDENTIFICATOR, SIR, NUMAR,

    // Keywords (Romanian)
    SI, CLASA, ALTFEL, FALS, FUNCTIE, PENTRU, DACA, NIMIC, SAU,
    SCRIE, INTOARCE, SUPER, ACESTA, ADEVARAT, VARIABILA, CAT_TIMP,

    //vad daca il folosesc
    PROCEDURA,

    EOF
}
