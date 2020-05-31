# ParserTongue
A recursive descent parser generator. First written in python, later rewritten in Java.

## EBNF
The grammar files are written in (a slightly extended) EBNF. The only extensions I use that are not in default EBNF are external rules and regex rules. External rules take an identifier from another grammar and use it in the current one, and regex rules create a regex object (re in python and java.util.regex.Pattern in java) based on the terminal you supply. 

For a basic crash course in EBNF:
1. Grammars are made up of one or more rules.
2. A rule is made up of a lhs and a rhs with some extra semantic tokens like '=' and ';'
3. A lhs is an identifier, which is a non-quoted string of alphanumeric characters and underscores that starts with a letter.
4. A rhs is one of the following:
    * an identifier
    * a terminal (which is a quoted string of characters)
    * an optional rhs
    * a repeated rhs
    * a grouped rhs (mostly used for clarity or to change precedence of | and ,)
    * a list of rhs alternatives
    * a list of concatentated rhs's.

There are several example .ebnf files scattered around this repository that you could look at for examples. (The ones in the java section are probably more up to date than the python ones).

## Using the code
The implementations in both languages should be very similar. The primary difference between them is that the python code makes heavy use of generators, which make the resulting code much cleaner. Since java has nothing like python's generators, those where all written in terms of iterators (or, things that are basically iterators).

The entry point you will want is (probably) either the Grammar object or the Parser object. The Parser object is really just a wrapper around a Grammar but it uses the dependency management code to link the main grammar to its dependencies (if any).

You can look at the testing/example code in both java and python to get an idea of what to do with your Grammar or Parser objects, but here is the basic idea:

### Checking if a string is in a language
The first step is to write at least one .ebnf file for your grammar (you could write more than one and include them into the main grammar file). Once you have that, pass it to either your Gramamar or Parser object. If you ended up with more than one grammar file, and need to manage dependencies, I would suggest using a Parser object rather than a Grammar object. If you truly want a Grammar, the Parser classes have a getGrammar() method to give you back the main Grammar.

If you have a parser, then all you have to do at this point is call Parser.checkFile() or Parser.checkRaw() in python, or Parser::checkFile or Parser::checkString in java depending on if the thing you are trying to parse is in a file or a string. However, the file version of these calls will read the entire file into a string to be tokenized, so might fail on huge files.

If you have a Grammar, then at this point you will need to create a Tokenizer (make sure to give it an appropriate TokenizerTypeList!), use the Tokenizer::tokenize method to tokenize your string, and then call Grammar::isInLanguage passing in your Tokenizer.

### Parsing a string into an object
Again, the first step is to write some grammar files and use them to create either a Grammar or a Parser.

Next you will want to create the classes / functions that will be used to set up your rule transformers. To use the python json parser as an example, I created a JsonArray class, and some functions, and then called Parser.setRuleTransform() to set the transformations. If you have a Grammar, you will want Grammar.setRuleTransformer() (same function names in java).

Once the rule transformers are set up, you then call Grammar.tryMatch() (passing a Tokenizer), Parser.parseFile(), or Parser.parseRaw() in python or Grammar::tryParse, Parser::parseFile, or Parser::parseString in java.
