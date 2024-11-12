import java.util.Scanner;
import java.util.Stack;

public class BrainfuckInterpreter {
	public static void main(String args[]) throws Exception{
		Scanner in = new Scanner(System.in);
		StringBuilder prog = new StringBuilder();

		prog.append(in.nextLine().trim().replace("\n", "")); // get all the code for Brainfuck

		byte memory[] = new byte[65536];
		int index = 0;
		Stack<Integer> loop = new Stack<>();

		final String program = prog.toString();
		prog = new StringBuilder(); // the output of the program.

		for(int i = 0; i < program.length(); i++) {
			if(index <= -1) index = 65536 - 1;
			if(index >= 65536) index = 0;

			char c = program.charAt(i);

			//System.out.println("Current Character: " + c + ", Current memory: " + (memory[index] + 0));
			//Thread.sleep(10);
			switch(c) {
				case '+':
					memory[index]++;
					break;
				case '-':
					memory[index]--;
					break;
				case '>':
					index++;
					break;
				case '<':
					index--;
					break;
				case ',':
					memory[index] = (byte)in.next().charAt(0);
					break;
				case '[': {
					loop.push(i);
					break;
				}
				case ']': {
					if(memory[index] != 0)
						i = loop.peek();
					else
						loop.pop();
					break;
				}
				case '.':
					  prog.append((char)memory[index]);
					  break;
				default: {
					System.out.println("Unrecognised character. Please check index " + i);
					System.exit(1);
				}
			}
		}
		System.out.println("Program output: " + prog.toString());
	}	
}
