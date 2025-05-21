grammar JSON;
@header {
package model.parser;
}

document: element EOF;

element:
    value=primitive #literal
    | array=collection #array
    | object=composite #object;

composite: '{' (property (',' property)*)? '}';

property: STRING ':' element;

primitive:
    string=STRING #string
    | number=NUMBER #number
    | boolean=BOOLEAN #boolean
    | NULL #null;

collection: '[' (element (',' element)*)? ']';

STRING: '"' (ESC | SAFECODEPOINT)* '"';

fragment ESC: '\\' (['"\\/bfnrt] | UNICODE);
fragment UNICODE: 'u' HEX HEX HEX HEX;
fragment HEX: [0-9a-fA-F];
fragment SAFECODEPOINT: ~['"\\\u0000-\u001F];

NUMBER: '-'? INT ('.' [0-9]+)? EXP?;

fragment INT: '0' | [1-9] [0-9]*;
fragment EXP: [Ee] [+\-]? INT;

BOOLEAN: 'true' | 'false';

NULL: 'null';

WS: [ \t\n\r]+ -> skip;