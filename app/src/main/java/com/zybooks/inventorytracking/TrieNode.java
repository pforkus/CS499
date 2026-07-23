package com.zybooks.inventorytracking;

import java.util.HashMap;


public class TrieNode {
    private final HashMap<Character, TrieNode> children;
    private boolean isWord;
    private String originalName; // Only set when isWord = true

    // Track duplicate names so delete wont remove if multiple matching entries exist
    private int count;

    public TrieNode() {
        children = new HashMap<>();
        isWord = false;
        originalName = null;
        count = 0;
    }

    // Getters
    public HashMap<Character, TrieNode> getChildren() {
        return children;
    }

    public boolean isWord() {
        return isWord;
    }

    public String getOriginalName() {
        return originalName;
    }

    public int getCount() {
        return count;
    }

    // Setters
    public void setWord(boolean isWord) {
        this.isWord = isWord;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
