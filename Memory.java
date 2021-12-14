// Jerry Xu
// CS 4348 Project 1 (CPU/Memory Simulator)

import java.io.*;
import java.util.Scanner;

public class Memory {
    public static int[] Memory = new int[2000];

    public static void main(String[] args) {
        // input file, sent by CPU process
        String file = args[0];

        // read input file into memory
        try {
            Scanner scan = new Scanner(new File(file));
            int i = 0;
            while(scan.hasNext()) {
                // if integer found then write to memory array
                if (scan.hasNextInt()) {
                    Memory[i] = scan.nextInt();
                    i++;
                } else {
                    String temp = scan.next();
                    // jump to specified location if line starts with '.'
                    if (temp.charAt(0) == '.') {
                        i = Integer.parseInt(temp.substring(1));
                    } else {
                        scan.nextLine();
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // IPC stuff
        Scanner sc = new Scanner(System.in);
        String input;

        // get input from CPU
        while (true) {
            input = sc.nextLine();
            if (input.equals("quit")) {
                break;
            }

            // read
            if (input.charAt(0) == 'r') {
                int address = Integer.parseInt(input.substring(1));
                // send requested data to CPU
                System.out.println(Memory[address]);
                System.out.flush();
                continue;
            }

            // write
            if (input.charAt(0) == 'w') {
                String[] line = input.substring(1).split(" ");
                int address = Integer.parseInt(line[0]);
                int data = Integer.parseInt(line[1]);
                Memory[address] = data;
            }
        }
    }
}