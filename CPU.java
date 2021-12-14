// Jerry Xu
// CS 4348 Project 1 (CPU/Memory Simulator)

import java.io.*;
import java.util.Scanner;
import java.lang.Runtime;

public class CPU {
    // CPU registers
    public static int PC = 0, SP = 999, IR, AC, X, Y;

    // for IPC
    public static PrintWriter pw;
    public static Scanner sc;
    public static InputStream is;
    public static OutputStream os;

    // flag indicating user mode or kernel mode
    public static boolean userMode = true;

    // count of instructions executed since an interrupt
    public static int instructionCount = 0;

    // command line arguments
    public static String inputFile;
    public static int timer;

    // fetches an instruction and increments the PC
    public static int fetchInstruction() {
        int instruction = readFromMemory(PC);
        PC++;
        return instruction;
    }

    // checks if CPU is trying to access system memory when not in kernel mode
    public static void accessSysMemory(int address) {
        if (userMode && address >= 1000) {
            System.out.println("Memory violation: accessing system address " + address + " in user mode");
            System.exit(0);
        }
    }

    // reads data from the specified memory address
    public static int readFromMemory(int address) {
        // check if attempting to access system memory
        accessSysMemory(address);
        //Send read command to Memory and store result.
        pw.println("r" + address);
        pw.flush();
        return Integer.parseInt(sc.nextLine());
    }

    // writes data to the specified memory address
    public static void writeToMemory(int address, int data) {
        accessSysMemory(address);
        pw.println("w" + address + " " + data);
        pw.flush();
    }

    // pushes data onto the stack
    public static void push(int data) {
        writeToMemory(SP, data);
        SP--;
    }

    // pops data from the stack
    public static int pop() {
        SP++;
        return readFromMemory(SP);
    }

    // executes an instruction
    public static void executeInstruction() {
        int value;
        switch (IR) {
            case 1:
                // load value into AC
                AC = fetchInstruction();
                break;
            case 2:
                // load value at the address into the AC
                AC = readFromMemory(fetchInstruction());
                break;
            case 3:
                // load value from address found in address
                AC = readFromMemory(readFromMemory(fetchInstruction()));
                break;
            case 4:
                // load value at addr + X
                AC = readFromMemory(fetchInstruction() + X);
                break;
            case 5:
                // load value at addr + Y
                AC = readFromMemory(fetchInstruction() + Y);
                break;
            case 6:
                // Load value at SP + X
                AC = readFromMemory(SP + X + 1);
                break;
            case 7:
                // store AC value into given address
                writeToMemory(fetchInstruction(), AC);
                break;
            case 8:
                // store random int[1-100] in AC
                AC = (int)(Math.random() * 100) + 1;
                break;
            case 9:
                // write AC as an int/char to the screen
                value = fetchInstruction();
                if (value == 1)
                    System.out.print(AC);
                if (value == 2)
                    System.out.print((char) AC);
                break;
            case 10: // add X to the AC
                AC += X;
                break;
            case 11:
                // add Y to the AC
                AC += Y;
                break;
            case 12:
                // subtract X from the AC
                AC -= X;
                break;
            case 13:
                // subtract Y from the AC
                AC -= Y;
                break;
            case 14:
                // copy AC to X
                X = AC;
                break;
            case 15:
                // copy X to AC
                AC = X;
                break;
            case 16:
                // copy AC to Y
                Y = AC;
                break;
            case 17:
                // copy Y to AC
                AC = Y;
                break;
            case 18:
                // copy AC to SP
                SP = AC;
                break;
            case 19:
                // copy SP to the AC
                AC = SP;
                break;
            case 20:
                // unconditional jump
                PC = fetchInstruction();
                break;
            case 21:
                // jump if AC = 0
                value = fetchInstruction();
                if (AC == 0) {
                    PC = value;
                }
                break;
            case 22:
                // jump if AC != 0
                value = fetchInstruction();
                if (AC != 0) {
                    PC = value;
                }
                break;
            case 23:
                // push return addr onto stack and jump there
                value = fetchInstruction();
                push(PC);
                PC = value;
                break;
            case 24:
                // pop return address from stack and jump there
                PC = pop();
                break;
            case 25:
                // increment X
                X++;
                break;
            case 26:
                // decrement X
                X--;
                break;
            case 27:
                // push AC onto stack
                push(AC);
                break;
            case 28:
                // pop AC from stack
                AC = pop();
                break;
            case 29:
                // perform system call
                interrupt('S');
                break;
            case 30:
                // return from system call
                PC = pop();
                SP = pop();
                userMode = true;
                instructionCount = 0;
                break;
            case 50:
                // end execution
                break;
        }
        // increment instructionCount if CPU is in user mode and not returning from syscall
        if (userMode && IR != 30) {
            instructionCount++;
        }
    }

    // processes an interrupt
    public static void interrupt(char type) {
        // set CPU to kernel mode
        userMode = false;
        // save previous stack pointer
        int prevSP = SP;
        // push previous stack pointer and PC onto the stack
        SP = 1999;
        push(prevSP);
        push(PC);
        // set the PC to proper memory address depending on interrupt type
        // T = timer interrupt, S = system call
        if (type == 'T') {
            PC = 1000;
        } else if (type == 'S') {
            PC = 1500;
        }
    }

    public static void main(String[] args) {
        inputFile = args[0];
        timer = Integer.parseInt(args[1]);

        try {
            // start the Memory process and streams for IPC
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("java Memory " + inputFile);
            is = proc.getInputStream();
            os = proc.getOutputStream();
            pw = new PrintWriter(os);
            sc = new Scanner(is);

            // the CPU's instruction cycle is the following:
            // 1. fetch the instruction in the IR
            // 2. decode and execute the instruction
            // 3. handle interrupts (if needed)
            // 4. Repeat
            while (true) {
                IR = fetchInstruction();
                // check if the end instruction was fetched
                if (IR == 50) {
                    break;
                }
                executeInstruction();
                // check if a timer interrupt should occur
                if (userMode && instructionCount > timer) {
                    interrupt('T');
                }
            }

            // terminate the Memory process
            pw.println("quit");
            pw.flush();
            proc.waitFor();
            int exitVal = proc.exitValue();
            System.out.println("Process exited: " + exitVal);

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
