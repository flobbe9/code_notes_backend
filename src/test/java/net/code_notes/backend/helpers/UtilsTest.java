package net.code_notes.backend.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * @since 1.0.0
 */
public class UtilsTest {
    
    @Test
    void paginate_shouldThrowIfNullArgsOrNegativeArgs() {
        assertThrows(IllegalArgumentException.class, () -> Utils.paginate(null, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> Utils.paginate(List.of(), -1, 0));
        assertThrows(IllegalArgumentException.class, () -> Utils.paginate(List.of(1), 0, -1));
        
    }

    @Test
    void paginate_shouldReturnEmptyList() {
        List<Integer> list = List.of();
        int pageIndex = 0;
        int pageSize = 5;

        // empty list
        assertTrue(pageSize > 0);
        assertTrue(list.isEmpty());
        assertTrue(Utils.paginate(list, pageIndex, pageSize).isEmpty());

        // page size 0
        pageSize = 0;
        list = List.of(1, 2, 3, 4, 5, 6);
        assertEquals(0, pageSize);
        assertFalse(list.isEmpty());
        assertTrue(Utils.paginate(list, pageIndex, pageSize).isEmpty());

        // page index out of bounds
        pageIndex = 2;
        pageSize = 5;
        assertTrue(pageSize > 0);
        assertFalse(list.isEmpty());
        assertTrue(list.size() <= (pageIndex * pageSize));
        assertTrue(Utils.paginate(list, pageIndex, pageSize).isEmpty());
    }

    @Test
    void paginate_shouldHandleEndIndexOutOfBoundsGracefully() {
        List<Integer> list = List.of(1, 2, 3);
        int pageIndex = 0;
        int pageSize = 5;

        // first page
        int endIndex = pageIndex * pageSize + pageSize;
        assertTrue(list.size() < endIndex);
        assertEquals(list.size() % pageSize, Utils.paginate(list, pageIndex, pageSize).size());

        // higher page
        pageIndex = 1;
        list = List.of(1, 2, 3, 4, 5, 6, 7);
        endIndex = pageIndex * pageSize + pageSize;
        assertTrue(pageIndex > 0);
        assertTrue(list.size() < endIndex);
        assertEquals(list.size() % pageSize, Utils.paginate(list, pageIndex, pageSize).size());
    }

    @Test
    void paginate_shouldApplyStartIndexAndPageSizeCorrectly() {
        List<Integer> list = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
        int pageIndex = 0;
        int pageSize = 5;

        int startIndex = pageIndex * pageSize;
        assertTrue(pageIndex == 0);
        // num results
        assertEquals(pageSize, Utils.paginate(list, pageIndex, pageSize).size());
        // start element
        assertEquals(list.get(startIndex), Utils.paginate(list, pageIndex, pageSize).get(0));

        pageIndex = 2;
        startIndex = pageIndex * pageSize;
        // num results
        assertEquals(list.size() % pageSize, Utils.paginate(list, pageIndex, pageSize).size());
        // start element
        assertEquals(list.get(startIndex), Utils.paginate(list, pageIndex, pageSize).get(0));
    }
}
