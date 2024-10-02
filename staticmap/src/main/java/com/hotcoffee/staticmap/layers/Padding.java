package com.hotcoffee.staticmap.layers;

/**
 * The padding to apply to the given {@link com.hotcoffee.staticmap.StaticMap}
 *
 * @param top    padding at the top
 * @param bottom padding at the bottom
 * @param left   padding at the left
 * @param right  padding at the right
 */
public record Padding(int top, int bottom, int left, int right) {
}
