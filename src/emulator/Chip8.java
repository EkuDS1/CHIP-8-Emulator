package emulator;

import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.io.File;        
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import static org.lwjgl.glfw.GLFW.*;

public class Chip8 {

	public static boolean drawFlag = true;
	private char opcode;
	private byte[] memory;				// 4096 bytes of main memory
	private byte[] V;					// general purpose registers
	private char I;						// index register
	private char pc; 					// program counter
	static int[] gfx;					// screen pixels
	byte delay_timer;
	byte sound_timer;

	private char[] stack;
	private char sp; 					// stack pointer

	static byte[] key = new byte[16]; 	// Key states stored from a Hex-based keypad

	private byte[] rom; // our program in hex codes



	public Chip8() {
		pc = (char) 0x200;	// Program Counter begins at 0x200
		opcode = 0;			// Reset opcode
		I = 0;				// Reset index register
		sp = 0;				// Reset stack pointer

		gfx = new int[64 * 32]; 	// Clear display
		stack = new char[16]; 		// Clear stack
		V = new byte[16];			// Clear registers V0-VF
		memory = new byte[4096];	// Clear memory

		int[] chip8_fontset =
			{ 
					0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
					0x20, 0x60, 0x20, 0x20, 0x70, // 1
					0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
					0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
					0x90, 0x90, 0xF0, 0x10, 0x10, // 4
					0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
					0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
					0xF0, 0x10, 0x20, 0x40, 0x40, // 7
					0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
					0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
					0xF0, 0x90, 0xF0, 0x90, 0x90, // A
					0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
					0xF0, 0x80, 0x80, 0x80, 0xF0, // C
					0xE0, 0x90, 0x90, 0x90, 0xE0, // D
					0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
					0xF0, 0x80, 0xF0, 0x80, 0x80  // F
			};
		for(int i = 0; i < chip8_fontset.length; i++)	// load fontset into memory
			memory[0x50 + i] = (byte) (chip8_fontset[i]);

		delay_timer = 0;	// Reset delay timer
		sound_timer = 0;	// Reset sound timer
	}

	public void loadGame() {
		try {
			try {
				// Set cross-platform Java L&F (also called "Metal")
				UIManager.setLookAndFeel(
						UIManager.getSystemLookAndFeelClassName());
			} 
			catch (UnsupportedLookAndFeelException e) {
				// handle exception
			}
			catch (ClassNotFoundException e) {
				// handle exception
			}
			catch (InstantiationException e) {
				// handle exception
			}
			catch (IllegalAccessException e) {
				// handle exception
			}

			File selectedFile = null;
			JFileChooser fileChooser = new JFileChooser();
			
			fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir") + "/c8programs"));
			int result = fileChooser.showOpenDialog(null);
			if (result == JFileChooser.APPROVE_OPTION)
				selectedFile = fileChooser.getSelectedFile();
			else System.exit(0);

			rom = Files.readAllBytes(Paths.get(selectedFile.getAbsolutePath()));	// load rom bytes from file
			System.arraycopy(rom, 0, memory, pc, rom.length);	// load rom into memory
		} catch (IOException e) {
			System.err.println("ERROR! Could not load game!");
			e.printStackTrace();
		}
	}

	public void emulateCycle() {
		// Fetch opcode by bitwise ORing two bytes (since a single instruction is two bytes long)
		opcode = (char) ((memory[pc] & 0xFF) << 8 | (memory[pc + 1] & 0xFF));

		// Decode opcode and execute it using the following switch statement
		switch (opcode & 0xF000) {

		case 0x0000:
			switch (opcode & 0x000F) {
			case 0x0000: // 0x00E0 Clears the screen
				gfx = new int[64 * 32];
				pc += 2;
				break;
			case 0x000E: // 0x00EE Returns from subroutine
				pc = stack[--sp]; // Decrement stack pointer and store address in program counter
				pc += 2;
				break;
			default:
				//System.err.printf("Unknown opcode [0x0000]: 0x%X\n", (int) opcode);
				break;
			}
			break;

		case 0x1000:	// 0x1NNN Jumps to address NNN
			pc = (char) (opcode & 0x0FFF);
			break;
		case 0x2000:	// 0x2NNN Calls subroutine at NNN
			stack[sp++] = pc;	// Store program counter in stack and increment stack pointer
			pc = (char) (opcode & 0x0FFF);
			break;
		case 0x3000:	// 0x3XNN Skips the next instruction if VX equals NN
			if (getV((opcode & 0x0F00) >>> 8) == (opcode & 0x00FF)) 
				pc += 4;
			else 
				pc += 2;
			break;
		case 0x4000:	// 0x4XNN Skips the next instruction if VX does not equal NN
			if (getV((opcode & 0x0F00) >>> 8) != (opcode & 0x00FF))
				pc += 4;
			else
				pc += 2;
			break;
		case 0x5000:	// 0x5XY0 Skips the next instruction if VX equals VY
			if (getV((opcode & 0x0F00) >>> 8) == getV((opcode & 0x00F0) >>> 4))
				pc += 4;
			else
				pc += 2;
			break;
		case 0x6000:	// 0x6XNN Sets VX to NN
			V[(opcode & 0x0F00) >>> 8] = (byte) (opcode & 0x00FF);
			pc += 2;
			break;
		case 0x7000:	// 0x7XNN Adds NN to VX (Carry flag is not changed)
			V[(opcode & 0x0F00) >>> 8] += (byte) (opcode & 0x00FF);
			pc += 2;
			break;
		case 0x8000:
			switch (opcode & 0x000F) {
			case 0x0000: // 0x8XY0 Sets VX to the value of VY
				V[(opcode & 0x0F00) >>> 8] = (byte) getV((opcode & 0x00F0) >>> 4);
				pc += 2;
				break;
			case 0x0001: // 0x8XY1 Sets VX to VX OR VY (Bitwise OR)
				V[(opcode & 0x0F00) >>> 8] |= getV((opcode & 0x00F0) >>> 4);
				pc += 2;
				break;
			case 0x0002: // 0x8XY2 Sets VX to VX AND VY (Bitwise AND)
				V[(opcode & 0x0F00) >>> 8] &= getV((opcode & 0x00F0) >>> 4);
				pc += 2;
				break;
			case 0x0003: // 0x8XY3 Sets VX to VX XOR VY
				V[(opcode & 0x0F00) >>> 8] ^= getV((opcode & 0x00F0) >>> 4);
				pc += 2;
				break;
			case 0x0004: // 0x8XY4 Adds VY to VX. VF is the carry.
				if(getV((opcode & 0x00F0) >>> 4) > (0xFF - getV((opcode & 0x0F00) >>> 8)))
					V[0xF] = 1; //carry
				else
					V[0xF] = 0;
				V[(opcode & 0x0F00) >>> 8] += getV((opcode & 0x00F0) >>> 4);
				pc += 2;          
				break;
			case 0x0005: // 0x8XY5 Subtracts VY from VX. If there is a borrow, VF is 0. Else, VF is 1
				if (getV((opcode & 0x0F00) >>> 8) < getV((opcode & 0x00F0) >>> 4))
					V[0xF] = 0;
				else
					V[0xF] = 1;
				V[(opcode & 0x0F00) >>> 8] -= getV((opcode & 0x00F0) >>> 4);
				pc += 2;
				break;
			case 0x0006: // 0x8XY6 Stores the LSB of VX in VF and then shifts VX to the right by 1
				V[0xF] = (byte) (getV((opcode & 0x0F00) >>> 8) & 0x0F);
				V[(opcode & 0x0F00) >>> 8] >>>= 1;
				pc += 2;
				break;
			case 0x0007: // 0x8XY7 Stores VY - VX in VX. If there is a borrow, VF is 0. Else, VF is 1
				if (getV((opcode & 0x00F0) >>> 4) < getV((opcode & 0x0F00) >>> 8))
					V[0xF] = 0;
				else
					V[0xF] = 1;
				V[(opcode & 0x0F00) >>> 8] = (byte) (getV((opcode & 0x00F0) >>> 4) - getV((opcode & 0x0F00) >>> 8));
				pc += 2;
				break;
			case 0x000E: // 0x8XYE Stores the MSB of VX in VF and then shifts VX to the left by 1
				V[0xF] = (byte) (getV((opcode & 0x0F00) >>> 8) & 0xF0);
				V[(opcode & 0x0F00) >>> 8] <<= 1;
				pc += 2;
				break;
			}
			break;
		case 0x9000:	// 0x9XY0 Skips the next instruction if VX is not equal to VY
			if (getV((opcode & 0x0F00) >>> 8) != getV((opcode & 0x00F0) >>> 4))
				pc += 4;
			else
				pc += 2;
			break;
		case 0xA000:	// 0xANNN Sets I to address NNN
			I = (char) (opcode & 0x0FFF);
			pc += 2;
			break;
		case 0xB000:	// 0xBNNN Jumps to the address NNN plus V0
			pc = (char) ((opcode & 0x0FFF) + (V[0] & 0xFF));
			break;
		case 0xC000:	// 0xCXNN Sets VX to the result of random number from 0-255 & NN
			V[(opcode & 0x0F00) >>> 8] = (byte) ((new Random().nextInt(256)) & (opcode & 0x00FF)) ;
			pc += 2;
			break;
		case 0xD000:	// 0XDXYN Draws sprite at coords (X,Y) with height N pixels and length 8 pixels
			char x = (char) getV((opcode & 0x0F00) >>> 8);
			char y = (char) getV((opcode & 0x00F0) >>> 4);


			char height = (char) (opcode & 0x000F);
			char pixel;

			int pixelLocation;

			V[0xF] = 0;

			for (int yline = 0; yline < height; yline++)
			{
				pixel = (char) memory[I + yline];
				for (int xline = 0; xline < 8; xline++) {
					if((pixel & (0x80 >>> xline)) != 0)
					{
						// pixel location in array = ((max - max_x) + x - ( y * max_x)) % max 
						// max - max_x = 2048 - 64 = 1984
						pixelLocation = (1984 + x + xline - ((y + yline) * 64)) % 2048;
						if (pixelLocation < 0)
							pixelLocation += 2048;

						if(gfx[pixelLocation] == 0xFFFFFFFF)
							V[0xF] |= (byte)1;                                
						gfx[pixelLocation] ^= 0xFFFFFFFF;
					}
				}
			}

			drawFlag = true;
			pc += 2;
			break;
		case 0xE000:	
			switch (opcode & 0x000F) {
			case 0x000E:	// 0xEX9E Skips the next instruction if the key in VX is pressed
				if (key[getV((opcode & 0x0F00) >>> 8)] != 0)
					pc += 4;
				else
					pc += 2;
				break;
			case 0x0001:	// 0xEXA1 Skips the next instruction if the key in VX isn't pressed
				if (key[getV((opcode & 0x0F00) >>> 8)] == 0)
					pc += 4;
				else
					pc += 2;
				break;
			}
			break;
		case 0xF000:
			switch (opcode & 0x00FF) {
			case 0x0007:	// 0xFX07  Sets VX to the value of the delay timer
				V[(opcode & 0x0F00) >>> 8] = delay_timer;
				pc += 2;
				break;
			case 0x000A:	// 0xFX0A  A key press is awaited, and then stored in VX
				do {
					glfwWaitEvents();	// I'm not sure about this code but I think it works???
				}
				while (key[InputHandler.latestKey] == 0);

				V[(opcode & 0x0F00) >>> 8] = (byte) InputHandler.latestKey;

				pc += 2;
				break;
			case 0x0015:	// 0xFX15  Sets the delay timer to VX
				delay_timer = (byte) getV((opcode & 0x0F00) >>> 8);
				pc += 2;
				break;
			case 0x0018:	// 0xFX18  Sets the sound timer to VX
				sound_timer = (byte) getV((opcode & 0x0F00) >>> 8);
				pc += 2;
				break;
			case 0x001E:	// 0xFX1E  Adds VX to I
				if (I == 0x500 && getV((opcode & 0x0F00) >>> 8) == 0x90)
					I = 0x590;
				else
					I += getV((opcode & 0x0F00) >>> 8);
				pc += 2;
				break;
			case 0x0029:	// 0xFX29  Sets I to the location of the sprite for character in VX
				I = (char) (0x050 + (getV((opcode & 0x0F00) >>> 8) * 5));
				pc += 2;
				break;
			case 0x0033:	// 0xFX33  Stores BCD form of VX in I, I+1 and I+3
				memory[I]     = (byte) (getV((opcode & 0x0F00) >>> 8) / 100);
				memory[I + 1] = (byte) ((getV((opcode & 0x0F00) >>> 8) / 10) % 10);
				memory[I + 2] = (byte) ((getV((opcode & 0x0F00) >>> 8) % 100) % 10);
				pc += 2;
				break;
			case 0x0055:	// 0xFX55  Stores V0 to VX in memory starting from address I
				for (int i = 0; i <= ((opcode & 0x0F00) >>> 8); i++) {
					memory[I + i] = V[i];
				}
				pc += 2;
				break;
			case 0x0065:	// 0xFX65  Fills V0 to VX with values from memory starting from address I
				for (int i = 0; i <= ((opcode & 0x0F00) >>> 8); i++) {
					V[i] = memory[I + i];

				}
				pc += 2;
				break;

			default:
				System.err.printf("Error: Unrecognized opcode. 0x%02X", opcode);
				break;
			}
			break;
		}

	}

	int getV(int index) {
		return V[index] & 0xFF;
	}

}
