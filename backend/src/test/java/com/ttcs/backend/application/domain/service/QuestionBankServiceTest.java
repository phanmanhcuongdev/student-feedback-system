package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.QuestionBankEntry;
import com.ttcs.backend.application.port.in.admin.GetQuestionBankEntriesQuery;
import com.ttcs.backend.application.port.in.admin.QuestionBankEntryCommand;
import com.ttcs.backend.application.port.out.admin.ManageQuestionBankPort;
import com.ttcs.backend.application.port.out.admin.QuestionBankSearchPage;
import com.ttcs.backend.application.port.out.admin.QuestionBankSearchQuery;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuestionBankServiceTest {

    @Test
    void shouldCreateReusableQuestionBankEntry() {
        InMemoryQuestionBankPort port = new InMemoryQuestionBankPort();
        QuestionBankService service = new QuestionBankService(port);
        var result = service.create(new QuestionBankEntryCommand("Rate clarity", "rating", "Teaching"));

        assertEquals(1, result.id());
        assertEquals("Rate clarity", result.content());
        assertEquals("RATING", result.type());
        assertEquals("Teaching", result.category());
    }

    @Test
    void shouldRejectBlankQuestionBankContent() {
        QuestionBankService service = new QuestionBankService(new InMemoryQuestionBankPort());

        assertThrows(IllegalArgumentException.class, () ->
                service.create(new QuestionBankEntryCommand(" ", "RATING", null)));
    }

    @Test
    void shouldUpdateQuestionBankEntryAndPreserveActiveState() {
        InMemoryQuestionBankPort port = new InMemoryQuestionBankPort();
        QuestionBankService service = new QuestionBankService(port);
        var created = service.create(new QuestionBankEntryCommand("Rate clarity", "RATING", "Teaching"));

        var updated = service.update(created.id(), new QuestionBankEntryCommand("Rate structure", "text", "Course"));

        assertEquals(created.id(), updated.id());
        assertEquals("Rate structure", updated.content());
        assertEquals("TEXT", updated.type());
        assertEquals("Course", updated.category());
        assertEquals(true, updated.active());
    }

    @Test
    void shouldArchiveAndRestoreQuestionBankEntry() {
        InMemoryQuestionBankPort port = new InMemoryQuestionBankPort();
        QuestionBankService service = new QuestionBankService(port);
        var created = service.create(new QuestionBankEntryCommand("Rate clarity", "RATING", "Teaching"));

        var archived = service.setActive(created.id(), false);
        var restored = service.setActive(created.id(), true);

        assertEquals(false, archived.active());
        assertEquals(true, restored.active());
    }

    @Test
    void shouldListQuestionBankEntriesThroughOutputPort() {
        InMemoryQuestionBankPort port = new InMemoryQuestionBankPort();
        QuestionBankService service = new QuestionBankService(port);
        service.create(new QuestionBankEntryCommand("Rate clarity", "RATING", "Teaching"));

        var page = service.list(new GetQuestionBankEntriesQuery(null, null, null, true, 0, 20));

        assertEquals(1, page.items().size());
        assertEquals("Rate clarity", page.items().getFirst().content());
    }

    private static final class InMemoryQuestionBankPort implements ManageQuestionBankPort {
        private final List<QuestionBankEntry> entries = new ArrayList<>();
        private int nextId = 1;

        @Override
        public QuestionBankSearchPage loadPage(QuestionBankSearchQuery query) {
            List<QuestionBankEntry> filtered = entries.stream()
                    .filter(entry -> query.active() == null || entry.active() == query.active())
                    .toList();
            return new QuestionBankSearchPage(filtered, query.page(), query.size(), filtered.size(), filtered.isEmpty() ? 0 : 1);
        }

        @Override
        public Optional<QuestionBankEntry> loadById(Integer id) {
            return entries.stream().filter(entry -> entry.id().equals(id)).findFirst();
        }

        @Override
        public QuestionBankEntry save(QuestionBankEntry entry) {
            QuestionBankEntry saved = entry.id() == null
                    ? new QuestionBankEntry(
                    nextId++,
                    entry.content(),
                    entry.type(),
                    entry.category(),
                    entry.active(),
                    entry.createdAt(),
                    entry.updatedAt()
            )
                    : entry;

            for (int index = 0; index < entries.size(); index++) {
                if (entries.get(index).id().equals(saved.id())) {
                    entries.set(index, saved);
                    return saved;
                }
            }
            entries.add(saved);
            return saved;
        }
    }
}
