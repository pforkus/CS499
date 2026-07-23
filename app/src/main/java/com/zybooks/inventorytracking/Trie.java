package com.zybooks.inventorytracking;

import java.util.ArrayList;
import java.util.List;

/* Trie to store user names for search suggestions.
 * Populates on app load, is traversed for matching database entry as user types.
 * Clicking a suggested item uses the item's name to perform a database query. */
public class Trie {
    private final TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

    // Traverses the trie from the root node, matching characters with nodes. If a node
    // has no children, one is added and populated, and the process repeated until each element of the
    // char array is inserted
    public void insert(String name) {
        TrieNode current = root;

        for(char c : name.toCharArray())  {
            TrieNode next = current.getChildren().get(c);

            if (next == null) {
                next = new TrieNode();
                current.getChildren().put(c, next);
            }
            current = next; // Move to next node
        }

        current.setWord(true);
        current.setOriginalName(name);
        current.setCount(current.getCount() + 1); // Increment the word occurrence counter
    }

    public List<String> getSuggestions(String prefix) {
        if(prefix.isEmpty()) {
            return new ArrayList<>(); // Do not show anything if search field is empty
        }

        TrieNode current = root;
        for(char c : prefix.toCharArray()) {
            TrieNode next = current.getChildren().get(c);

            if(next == null) {
                return new ArrayList<>();
            }
            current = next;
        }

        List<String> results = new ArrayList<>();
        collectWords(current, results);
        return results;
    }

    private void collectWords(TrieNode node, List<String> results) {
        if(node.isWord()) {
            results.add(node.getOriginalName());
        }
        for(TrieNode child : node.getChildren().values()) {
            collectWords(child, results);
        }
    }

    public boolean delete(String name) {
        return deleteHelper(root, name, 0);
    }

    private boolean deleteHelper(TrieNode node, String name, int depth) {
        if(depth == name.length()) {
            if(!node.isWord()) return false;

            node.setCount(node.getCount() - 1); // Decrement count
            if(node.getCount() <= 0) {
                node.setWord(false);
                node.setOriginalName(null);
            }
            return !node.isWord() && node.getChildren().isEmpty();
        }

        char c = name.charAt(depth);
        TrieNode child = node.getChildren().get(c);
        if(child == null) return false; // name does not exist in trie

        boolean shouldDeleteCurrentNode = deleteHelper(child, name, depth + 1);

        if(shouldDeleteCurrentNode) {
            node.getChildren().remove(c);
            // Node can be pruned if its not a word, and has no other children
            return !node.isWord() && node.getChildren().isEmpty();
        }
        return false;
    }
}
