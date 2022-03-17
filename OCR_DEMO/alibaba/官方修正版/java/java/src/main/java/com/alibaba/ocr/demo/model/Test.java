package com.alibaba.ocr.demo.model;

public class Test {

    public static void main(String[] args) {
        String str = "321313eweq12313djsahd3213145465";
        System.out.println(findLongestNumSubstring(str));
    }

    public static String findLongestNumSubstring(String input) {
        // If the string is empty, return [0, 0] directly.
        if (input == null || input.length() == 0) {
            return null;
        }

        int index = 0;
        int[] ret = new int[]{0, 0}; //[start_index, length]
        int currLen = 0;
        while (index < input.length()) {
            currLen = 0;
            while (index < input.length() && Character.isDigit(input.charAt(index))) {
                currLen++;
                index++;
            }

            // If current substring is longer than or equal to the previously found substring
            // Put it in return values.
            if (currLen != 0 && ret[1] <= currLen) {
                ret[1] = currLen;
                ret[0] = index - currLen;
            }
            index++;
        }
        return input.substring(ret[0], ret[0] + ret[1]);
    }
}