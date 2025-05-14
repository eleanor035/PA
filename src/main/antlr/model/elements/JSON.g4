grammar JSON;

json: object | array;

value
    : object # ValueObject  // Changed from ObjectValue
    | array  # ValueArray   // Changed from ArrayValue
    | STRING
    | NUMBER
    | 'true'
    | 'false'
    | 'null'
    ;

object
    : '{' pair (',' pair)* '}' # ObjectDefinition  // Unique label
    | '{' '}' # EmptyObject
    ;

array
    : '[' value (',' value)* ']' # ArrayDefinition  // Unique label
    | '[' ']' # EmptyArray
    ;

// Remaining rules (STRING, NUMBER, etc.) unchanged

pair: STRING ':' value;

value
    : STRING    # StringValue
    | NUMBER    # NumberValue
    | 'true'    # TrueValue
    | 'false'   # FalseValue
    | 'null'    # NullValue
    | object    # ObjectValue
    | array     # ArrayValue
    ;

STRING: '"' (ESC | ~["\\])* '"';

fragment ESC: '\\' (["\\/bfnrt] | UNICODE);
fragment UNICODE: 'u' HEX HEX HEX HEX;
fragment HEX: [0-9a-fA-F];

NUMBER
    : '-'? INT ('.' [0-9]+)? EXP?
    ;

fragment INT: '0' | [1-9] [0-9]*;
fragment EXP: [Ee] [+\-]? INT;

WS: [ \t\n\r]+ -> skip;