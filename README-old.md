# Trick

Trick is a custom OOP language compiler written in Java. This project was to better understand how compilers are built and with added functionality we deemed would improve the language.

## Credits

Please note this language is built upon following the following source very closely and the credit for the basics functionality and design of the compiler goes to this source:

[Crafting Interpreters](https://craftinginterpreters.com/)

## Trick Language Documentation:
[Notion Page](https://www.notion.so/Java-Project-028727268099418687937b8b32efb279?pvs=4)
<div></div>
While most of the language follows conventional Java implementation there are notable differences

###   REPL Session Notes

If you wish to end your REPL session, enter "/0" in a line on its
and it will end the session.

### Type conversion

| Trick type     | Java Representation |
| -------------- | ------------------- |
| Any Lox value  | Object              |
| nil            | null                |
| Boolean        | Boolean             |
| Number         | Double              |
| String         | String              |
| Char           | Char                |

## Language Grammar

Statement:

program         → declaration* EOF ;  
declaration     → varDecl | statement ;  
varDecl         → "var" IDENTIFIER ( "=" expression ) ? ";" ;  
statement       → exprStmt | printStmt | block;  
exprStmt        → expression ";" ;  
printStmt       → "print" expression ";" ;  
block           → "{" declaration* "}" ;  

Expression:  

expression      → assignment;  
assignment      → ( IDENTIFIER "=" assignment ) | equality ;   
equality        → comparison ( ( "!=" | "==" ) comparison )* ;  
comparison      → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;  
term            → factor ( ( "-" | "+" ) factor )* ;  
factor          → unary ( ( "/" | "*" ) )* ;  
unary           → ( "!" | "-" ) unary | primary ;  
primary         → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" | IDENTIFIER;  



### Variable Declaration
Variables of any time are declared using "var" keyword operations can be used within declaration: i.e var result = 1 + 1

### Expressions
#### If-Else Expressions
      instead of && / || we will use keywords 'and' / 'or'
      due to the below section we can even use this for statments like below
i.e 
<div>
print "hi" or 2; // "hi". 
</div>

<div> 
print nil or "yes" //"yes".
</div>

#### Truth?
Any boolean false and nil value are considered false while all else is true

#### String/Char Manipulation
Arithemtic a + b also works to concatenate string and chars with any other string, char, or number to create a final string.

### Number Literals
When performing method calls on numbers, negation will not take precendence over the method i.e: print -123.abs(); --> -123

### Numbers/Arithmetic
All numbers are doubles
Supports checking for equality/inequality. (=/!=) between any types - i.e check for 3 = three (Reals on ##Truth? logic)

###   Statements
####  If-Else Statements
Else is always implicitly attached to latest if statment.
<div> 
e.g
</div>

```java
if(condition{} if(condition){} else{})
```
hence the else statement belongs to the second if condition

### Exit Codes
65: Syntax error while scanning
70: Computation error while running the interpreter 

---

### HEAD NOTES FOR DEVELOPERS:
- Any mention of "|*********|" indicates an area of improvement or advancement  needed for development
- Any mention of "|&&&&&&&&&| indicates uncertainties or additions needed for basic functionality
- Any mention of ?!?!?!?!?! indicates a new addition that needs to be tested

### IDEAS FOR IMPLEMENTATION:
- To quote Crafting Interpreters "Ideally, we would have an actual abstraction, some kind of “ErrorReporter” interface that gets passed to the scanner and parser so that we can swap out different reporting strategies." - may be an opportunity for AI to play a role

## Planning

- [x] Chapter 4 (Youssef)
- [x] Chapter 5 (Ratiq)
- [x] Chapter 6 (Ratiq)
- [x] Chapter 7 (Youssef)
- [x] Chapter 8 (Youssef)
- [x] Chapter 9 (Youssef)
- [ ] Chapter 10 (Ratiq)
- [ ] Chapter 11 (Youssef)

### Bonus functionality

- [x] Tokenize Chars
- [x] Unknown characters blob should be reported as one entity rather than individual i.e "これ は　だいじょぶない"
- [ ] Specific error reporting

### Testing

- [x] Scanner
- [ ] Parser
- [ ] Interpreter
- [ ] Error reporting
- [ ] Blob reporting
