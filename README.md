# Word Sense Disambiguation

By Gary Chen and Ryan Kwon

____

## Instructions:

The interface for interacting with the project is primarily driven through the shell (.sh) files. These have generally been configured for UNIX machines, but should also work for Windows machines.

In order to run the project, these commands should be executed in terminal:

`sh compile_all.sh` -> Compiles all Java files, setting the classpath appropriately to point at the bin and jar folder.

`sh run_program.sh` -> Reads in a subset of the Guardian data set and creates a data file `data.ser`. This process may take 3-4 minutes. Once `data.ser` exists however, `run_program.sh` will read and execute in under a minute. This will allow you to interact with the program after selecting a scoring method of your choice. This author strongly recommends Sentence Match.

After entering in a sentence as well as specifying the ambiguous word in the sentence, the program will then retrieve the top 10 closest sentences to your proposal from the training corpus.

`sh run_program_with_dict.sh` -> Reads in a subset of the Guardian data set, but retrieves definitions from WordNet rather than the Guardian text.

___

If you wish to run the TestData set, be sure to delete your local copy of `data.ser`. As `data.ser` will likely be serialized for the Guardian text, your program will not read in the TestData set.
