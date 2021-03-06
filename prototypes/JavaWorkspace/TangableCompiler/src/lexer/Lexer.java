package lexer;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Stack;

import ast.Command;
import ast.Keyword;
import ast.LiteralNumber;
import ast.Statement;
import core.LanguageDefinition;
import exceptions.SyntaxException;

/**
 * Lex's the supplied input file and produces a list of tokens
 * Instanced to the correct type to allow single look ahead
 * 
 * @author Paul Hickman
 * 
 */
public class Lexer {

	/**
	 * List of token objects created by lexing input file
	 */
	public static final Stack<Token> tokenList = new Stack<Token>();

	/**
	 * Clear lexer of all tokens
	 */
	public static void clear(){
		tokenList.clear();
	}
	
	/**
	 * Lex's supplied CSV file
	 * @param fileName - file to be lexed
	 * @return false is file was unable to be lexed correctly, true is successful
	 * @throws SyntaxException 
	 */
	public static boolean Lex(String fileName) throws SyntaxException{

		BufferedReader br = null;
		String tokenData = "";

		LanguageDefinition lang = LanguageDefinition.getInstance();

		try{
			br = new BufferedReader(new FileReader(fileName));

			while((tokenData = br.readLine()) != null){
				String[] tokens = tokenData.split(",");

				for(String s : tokens){

					Statement tokDef = lang.tokens.get(Integer.parseInt(s));

					if(tokDef != null){

						if(tokDef instanceof Command){
							tokenList.add((Command)tokDef);
						}else if(tokDef instanceof Keyword){
							tokenList.add((Keyword)tokDef);
						}else if(tokDef instanceof LiteralNumber){
							tokenList.add((LiteralNumber)tokDef);
						}

					}else{
						br.close();
						throw new SyntaxException();
					}
				}

			}

			br.close();
		}catch(FileNotFoundException fnfe){
			System.err.println(fnfe.getMessage());
			return false;
		} catch (IOException ioe) {
			System.err.println(ioe.getMessage());
			return false;
		}

		stackReversal(tokenList);

		return true;
	}

	/**
	 * Lex's supplied token string
	 * @param rawTokenString - token string
	 * @param seperator - String to be used to separate tokens
	 * @return false if the token string was unable to be lexed correctly, true if successful
	 * @throws SyntaxException 
	 */
	public static boolean Lex(String rawTokenString, String seperator) throws SyntaxException{

		LanguageDefinition lang = LanguageDefinition.getInstance();
		String[] tokens = rawTokenString.split(seperator);

		for(String s : tokens){

			Statement tokDef = lang.tokens.get(Integer.parseInt(s));

			if(tokDef != null){

				if(tokDef instanceof Command){
					tokenList.add((Command)tokDef);
				}else if(tokDef instanceof Keyword){
					tokenList.add((Keyword)tokDef);
				}else if(tokDef instanceof LiteralNumber){
					tokenList.add((LiteralNumber)tokDef);
				}

			}else{
				throw new SyntaxException();
			}
		}


		stackReversal(tokenList);

		return true;
	}
	
	/**
	 * Flip the token stack. Recursively called
	 * @param s
	 */
	private static void stackReversal(Stack<Token> s)
	{
		if(s.size() == 0) return;
		Token n = getLast(s);
		stackReversal(s);
		s.push(n);
	}

	/**
	 * Support function for "stackReversal"
	 * @param s
	 * @return
	 */
	private static Token getLast(Stack<Token> s)
	{
		Token t = s.pop();
		if(s.size() == 0)
		{
			return t;
		}
		else
		{
			Token k = getLast(s);
			s.push(t);
			return k;
		}
	}


	/**
	 * peek at the next token on the lexer stack
	 * @return
	 */
	public static Token peek(){
		if(tokenList.isEmpty())
			return null;

		return tokenList.peek();
	}

	/**
	 * pop the next token off the lexer stack
	 * @return
	 */
	public static Token pop(){
		if(tokenList.isEmpty())
			return null;

		return tokenList.pop();
	}

	/**
	 * Check if the lexer stack is empty
	 * @return false if empty, true if size 
	 */
	public static boolean isEmpty(){
		return tokenList.empty();
	}
}
