/*
Copyright (c) 2019, Michael Mollard
*/

grammar Query;

// syntactic rules
input
   : query EOF
   ;

query
   : left=query logicalOp=(AND | OR) right=query #opQuery
   | LPAREN query RPAREN #priorityQuery
   | criteria #atomQuery
   ;

criteria
   : key (IN | NOT_IN) array #eqArrayCriteria
   | key op value #opCriteria
   | key (BETWEEN | NOT_BETWEEN) left=value AND right=value #betweenCriteria
   | key (IS | IS_NOT) is_value #isCriteria
   ;

is_value
   : EMPTY
   | NULL
   ;

key
   : IDENTIFIER
   ;

array
   : LBRACKET (value (',' value)* )? RBRACKET
   ;

value
   : IDENTIFIER
   | STRING
   | ENCODED_STRING
   | NUMBER
   | BOOL
  ;

op
   : EQ
   | GT
   | GTE
   | LT
   | LTE
   | NOT_EQ
   ;

// lexical rules
BOOL
    : 'true'
    | 'false'
    ;

NULL
    : 'NULL'
    ;

STRING
 : '"' DoubleStringCharacter* '"'
 | '\'' SingleStringCharacter* '\''
 ;

fragment DoubleStringCharacter
   : ~["\\\r\n]
   | '\\' EscapeSequence
   | LineContinuation
   ;

fragment SingleStringCharacter
    : ~['\\\r\n]
    | '\\' EscapeSequence
    | LineContinuation
    ;

fragment EscapeSequence
    : CharacterEscapeSequence
    | HexEscapeSequence
    | UnicodeEscapeSequence
    ;

fragment CharacterEscapeSequence
 : SingleEscapeCharacter
 | NonEscapeCharacter
 ;

fragment HexEscapeSequence
 : 'x' HexDigit HexDigit
 ;
 
fragment UnicodeEscapeSequence
 : 'u' HexDigit HexDigit HexDigit HexDigit
 ;

fragment SingleEscapeCharacter
 : ['"\\bfnrtv]
 ;

fragment NonEscapeCharacter
 : ~['"\\bfnrtv0-9xu\r\n]
 ;

fragment EscapeCharacter
 : SingleEscapeCharacter
 | DecimalDigit
 | [xu]
 ;

fragment LineContinuation
 : '\\' LineTerminatorSequence
 ;

fragment LineTerminatorSequence
 : '\r\n'
 | LineTerminator
 ;

fragment DecimalDigit
 : [0-9]
 ;
fragment HexDigit
 : [0-9a-fA-F]
 ;
fragment OctalDigit
 : [0-7]
 ;

AND
   : 'AND'
   ;

OR
   : 'OR'
   ;

NUMBER
   : ('0' .. '9') ('0' .. '9')* POINT? ('0' .. '9')*
   ;

LPAREN
   : '('
   ;


RPAREN
   : ')'
   ;

LBRACKET
   : '['
   ;

RBRACKET
    : ']'
    ;

GT
   : '>'
   ;

GTE
   : '>:'
   ;

LT
   : '<'
   ;

LTE
   : '<:'
   ;

EQ
   : ':'
   ;

IS
   : 'IS'
   ;

IS_NOT
    : 'IS NOT'
    ;

EMPTY
   : 'EMPTY'
   ;

NOT_EQ
   : '!'
   ;

BETWEEN
   : 'BETWEEN'
   ;

NOT_BETWEEN
   : 'NOT BETWEEN'
   ;
IN
   : 'IN'
   ;

NOT_IN
   : 'NOT IN'
   ;

fragment POINT
   : '.'
   ;

IDENTIFIER
   : [A-Za-z0-9.]+
   ;

ENCODED_STRING //anything but these characters :<>!()[], and whitespace
   : ~([ ,:<>!()[\]])+
   ;


LineTerminator
: [\r\n\u2028\u2029] -> channel(HIDDEN)
;

WS
   : [ \t\r\n]+ -> skip
   ;