/**
 * Declarative windows: describe a screen in a few lines, without coordinates or a {@code Screen}
 * subclass.
 *
 * <p>Get a {@link com.galdurstudio.galdurui.api.window.GaldurWindow} from
 * {@link com.galdurstudio.galdurui.api.GaldurLib#createWindow()}, add elements, then open it.
 * Layout, theme and rendering are handled by the library. Every {@code add...} method returns a
 * handle that keeps the element state, before and after opening:</p>
 * <ul>
 *   <li>{@link com.galdurstudio.galdurui.api.window.ElementHandle}: enabled, visible, tooltip;</li>
 *   <li>{@link com.galdurstudio.galdurui.api.window.CheckboxHandle}: plus the checked state;</li>
 *   <li>{@link com.galdurstudio.galdurui.api.window.RowHandle}: elements side by side;</li>
 *   <li>{@link com.galdurstudio.galdurui.api.window.ListHandle}: lines and selection of a list;</li>
 *   <li>{@link com.galdurstudio.galdurui.api.window.SearchHandle}: text of a search field;</li>
 *   <li>{@link com.galdurstudio.galdurui.api.window.TabsHandle} and
 *       {@link com.galdurstudio.galdurui.api.window.TabHandle}: tabs, each filled like a window;</li>
 *   <li>{@link com.galdurstudio.galdurui.api.window.ProgressHandle}: progress and label of a bar.</li>
 * </ul>
 *
 * <p>Public API, stable within a major version.</p>
 */
package com.galdurstudio.galdurui.api.window;
