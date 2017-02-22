package lexer;

import lexer.Token.TokenClass;
import java.io.EOFException;
import java.io.IOException;

/**
 * @author cdubach
 */
public class Tokeniser {

    private Scanner scanner;

    private int error = 0;
    public int getErrorCount() {
	return this.error;
    }

    public Tokeniser(Scanner scanner) {
        this.scanner = scanner;
    }

    private void error(char c, int line, int col) {
        System.out.println("Lexing error: unrecognised character ("+c+") at "+line+":"+col);
        error++;
    }


    public Token nextToken() {
        Token result;
        try {
             result = next();
        } catch (EOFException eof) {
            // end of file, nothing to worry about, just return EOF token
            return new Token(TokenClass.EOF, scanner.getLine(), scanner.getColumn());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            // something went horribly wrong, abort
            System.exit(-1);
            return null;
        }
        return result;
    }

    /*
     * To be completed
     */

    private Token next() throws IOException {

        int line = scanner.getLine();
        int column = scanner.getColumn();

        // get the next character
        char c = scanner.next();

        // skip white spaces
        if (Character.isWhitespace(c))
            return next();

        // skip comment
        if (c =='/')
        {
            c = scanner.peek();
            if (c =='/')
            {
            	//scanner.setLine(line++);
            	//scanner.setColumn(0);
            	//scanner.next();
                scanner.next();
            	scanner.nextLine();
                return next();
            }
            else
            {
                if (c == '*')
                {
                    c = scanner.next();
                    while(true)
                    {
                    	c = scanner.next();
                    	if(c == '*')
                    	{
                    		c = scanner.next();
                    		if(c == '/')
                    		{
                    			break;
                    		}
                    	}
                    }
                    return next();
                }
                else
                {
                    return new Token(TokenClass.DIV, line, column);
                }
            }
        }


        // recognises the operator
        if (c == '+')
            return new Token(TokenClass.PLUS, line, column);
        if (c == '-')
            return new Token(TokenClass.MINUS, line, column);
        if (c == '*')
            return new Token(TokenClass.ASTERIX, line, column);
        //if (c == '/')
          //  return new Token(TokenClass.DIV, line, column);
        if (c == '%')
            return new Token(TokenClass.REM, line, column);

        // recognises the delimiters
        if (c == '{')
            return new Token(TokenClass.LBRA, line, column);
        if (c == '}')
            return new Token(TokenClass.RBRA, line, column);
        if (c == '(')
            return new Token(TokenClass.LPAR, line, column);
        if (c == ')')
            return new Token(TokenClass.RPAR, line, column);
        if (c == '[')
            return new Token(TokenClass.LSBR, line, column);
        if (c == ']')
            return new Token(TokenClass.RSBR, line, column);
        if (c == ';')
            return new Token(TokenClass.SC, line, column);
        if (c == ',')
            return new Token(TokenClass.COMMA, line, column);

        // recognises the struct member access
        if (c == '.')
            return new Token(TokenClass.DOT, line, column);

        // recognises the comparisons
        if (c == '=')
        {
            c = scanner.peek();
            if (c == '=')
            {
                scanner.next();
                return new Token(TokenClass.EQ, line, column);
            }
            else
            {
                return new Token(TokenClass.ASSIGN, line, column);
            }
        }
        if (c == '!')
        {
            c = scanner.peek();
            if (c == '=')
            {
                scanner.next();
                return new Token(TokenClass.NE, line, column);
            }
            else
            {
                error(c, line, column);
                return new Token(TokenClass.INVALID, line, column);
            }
        }
        if (c == '<')
        {
            c = scanner.peek();
            if (c == '=')
            {
                scanner.next();
                return new Token(TokenClass.LE, line, column);
            }
            else
            {
                return new Token(TokenClass.LT, line, column);
            }
        }
        if (c == '>')
        {
            c = scanner.peek();
            if (c == '=')
            {
                scanner.next();
                return new Token(TokenClass.GE, line, column);
            }
            else
            {
                return new Token(TokenClass.GT, line, column);
            }
        }

        //recognise the logical operators
        if (c == '&')
        {
            c = scanner.peek();
            if (c == '&')
            {
                scanner.next();
                return new Token(TokenClass.AND, line, column);
            }
            else
            {
                error(c, line, column);
                return new Token(TokenClass.INVALID, line, column);
            }
        }
        if (c == '|')
        {
            c = scanner.peek();
            if (c == '|')
            {
                scanner.next();
                return new Token(TokenClass.OR, line, column);
            }
            else
            {
                error(c, line, column);
                return new Token(TokenClass.INVALID, line, column);
            }
        }

        //recognise the identifier
        if (Character.isLetter(c) || c == '_')
        {
            StringBuilder sb = new StringBuilder();
            sb.append(c);
            c = scanner.peek();
            while (Character.isLetterOrDigit(c) || c == '_')
            {
                sb.append(c);
                scanner.next();
                c = scanner.peek();
            }
            switch(sb.toString())
            {
                case "if":
                return new Token(TokenClass.IF, line, column);
                case "else":
                return new Token(TokenClass.ELSE, line, column);
                case "while":
                return new Token(TokenClass.WHILE, line, column);
                case "return":
                return new Token(TokenClass.RETURN, line, column);
                case "struct":
                return new Token(TokenClass.STRUCT, line, column);
                case "sizeof":
                return new Token(TokenClass.SIZEOF, line, column);
                case "int":
                return new Token(TokenClass.INT, line, column);
                case "void":
                return new Token(TokenClass.VOID, line, column);
                case "char":
                return new Token(TokenClass.CHAR, line, column);
                default:
                break;
            }
            return new Token(TokenClass.IDENTIFIER, sb.toString(), line, column);
        }

        //recognise the literals
        if (Character.isDigit(c))
        {
            StringBuilder sb = new StringBuilder();
            sb.append(c);
            c = scanner.peek();
            while (Character.isDigit(c))
            {
                sb.append(c);
                scanner.next();
                c = scanner.peek();
            }
            return new Token(TokenClass.INT_LITERAL, sb.toString(), line, column);
        }

        if (c == '\'')
        {
            StringBuilder sb = new StringBuilder();
            c = scanner.peek();
            if (c != '\\' && c != '\'')
            {
                sb.append(c);
                scanner.next();
                c = scanner.peek();
                if (c =='\'')
                {
                    scanner.next();
                    return new Token(TokenClass.CHAR_LITERAL, sb.toString(), line, column);
                }
                else
                {
                    error(c, line, column);
                    return new Token(TokenClass.INVALID, line, column);
                }
            }
            else
            {
                if (c == '\\')
                {
                    sb.append(c);
                    scanner.next();
                    c = scanner.peek();
                    if (c == 't' || c == 'b' || c == 'n' || c == 'r' || c == 'f' || c == '\'' || c == '\"' || c == '\\')
                    {
                        sb.append(c);
                        scanner.next();
                        c = scanner.peek();
                        if (c =='\'')
                        {
                            scanner.next();
                            return new Token(TokenClass.CHAR_LITERAL, sb.toString(), line, column);
                        }
                        else
                        {
                            error(c, line, column);
                            return new Token(TokenClass.INVALID, line, column);
                        }
                    }
                    else
                    {
                        error(c, line, column);
                        return new Token(TokenClass.INVALID, line, column);
                    }
                }
                error(c, line, column);
                return new Token(TokenClass.INVALID, line, column);
            }
        }


        if (c == '\"')
        {
            StringBuilder sb = new StringBuilder();
            while(true)
            {
                c = scanner.peek();
                if (c == '\n' || c =='\r')
                {
                    error(c, line, column);
                    return new Token(TokenClass.INVALID, line, column);
                }
                if (c != '\\' && c != '\"')
                {
                    sb.append(c);
                    scanner.next();
                }
                else
                {
                    if (c == '\\')
                    {
                        sb.append(c);
                        c = scanner.next();
                        c = scanner.next();
                        sb.append(c);
                    }
                    else
                    {
                        if (c == '\"')
                        {
                            scanner.next();
                            return new Token(TokenClass.STRING_LITERAL, sb.toString(), line, column);
                        }
                    }
                }
            }
        }




        //recognise the include
        if (c == '#')
        {
            StringBuilder sb = new StringBuilder();
            sb.append(c);
            c = scanner.peek();
            while (Character.isLetterOrDigit(c))
            {
                sb.append(c);
                scanner.next();
                c = scanner.peek();
            }
            if(sb.toString().equals("#include"))
            {
            	return new Token(TokenClass.INCLUDE, line, column);
            }
        }
        // ... to be completed

        /*if (!scanner.hasNext())
        {
            return new Token(TokenClass.EOF, line, column);
        }*/


        // if we reach this point, it means we did not recognise a valid token
        error(c, line, column);
        return new Token(TokenClass.INVALID, line, column);
    }


}