package com.asrevo.cvhome.content.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.content.Content;
import com.asrevo.cvhome.content.entity.faq.ContentFaq;
import com.asrevo.cvhome.content.entity.faq.FaqGroup;
import com.asrevo.cvhome.content.entity.faq.FaqGroupDescription;
import com.asrevo.cvhome.content.entity.faq.FaqReference;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.errors.FaqGroupNotFoundException;
import com.asrevo.cvhome.content.errors.InvalidFaqReorderException;
import com.asrevo.cvhome.content.model.ContentType;
import com.asrevo.cvhome.content.model.ContentView;
import com.asrevo.cvhome.content.model.faq.FaqGroupView;
import com.asrevo.cvhome.content.model.faq.FaqGroupWriteRequest;
import com.asrevo.cvhome.content.model.faq.FaqReferenceSpec;
import com.asrevo.cvhome.content.model.faq.FaqReorderRequest;
import com.asrevo.cvhome.content.model.faq.FaqView;
import com.asrevo.cvhome.content.model.faq.FaqWriteRequest;
import com.asrevo.cvhome.content.repository.ContentFaqRepository;
import com.asrevo.cvhome.content.repository.ContentRepository;
import com.asrevo.cvhome.content.repository.FaqGroupRepository;

@Service
public class FaqService {
    private final ContentV2Service contentService;
    private final ContentRepository contentRepository;
    private final FaqGroupRepository groupRepository;
    private final ContentFaqRepository faqRepository;

    public FaqService(ContentV2Service contentService, ContentRepository contentRepository,
                      FaqGroupRepository groupRepository, ContentFaqRepository faqRepository) {
        this.contentService = contentService;
        this.contentRepository = contentRepository;
        this.groupRepository = groupRepository;
        this.faqRepository = faqRepository;
    }

    @Transactional
    public FaqGroupView createGroup(StoreMerchantId store, LanguageCode language, FaqGroupWriteRequest request) {
        FaqGroup group = new FaqGroup();
        group.setStoreMerchantId(store);
        group.setCode(request.code());
        group.setPosition(request.position());
        FaqGroupDescription description = new FaqGroupDescription();
        description.setLanguageCode(language);
        description.setName(request.name());
        group.addDescription(description);
        return toView(groupRepository.save(group), language);
    }

    @Transactional(readOnly = true)
    public List<FaqGroupView> listGroups(StoreMerchantId store, LanguageCode language) {
        return groupRepository.findAllByStoreMerchantIdOrderByPositionAscIdAsc(store).stream()
                .map(it -> toView(it, language)).toList();
    }

    @Transactional
    public FaqView create(StoreMerchantId store, LanguageCode language, FaqWriteRequest request, String actor)
            throws FaqGroupNotFoundException, ContentNotFoundException, InvalidFaqReorderException {
        if (request.content().type() != ContentType.FAQ) {
            throw InvalidFaqReorderException.because("content-type");
        }
        FaqGroup group = groupRepository.findForUpdateByIdAndStoreMerchantId(request.groupId(), store)
                .orElseThrow(() -> FaqGroupNotFoundException.forId(request.groupId()));
        List<ContentFaq> entries = faqRepository
                .findAllByGroupIdAndContentStoreMerchantIdOrderByPositionAscIdAsc(group.getId(), store);
        int position = Math.min(request.position(), entries.size());
        shift(entries, position);
        ContentView contentView = contentService.create(store, language, request.content(), actor);
        Content content = contentRepository.findByIdAndStoreMerchantId(contentView.id(), store)
                .orElseThrow(() -> ContentNotFoundException.forId(contentView.id()));
        ContentFaq faq = new ContentFaq();
        faq.setContent(content);
        faq.setGroup(group);
        faq.setPosition(position);
        for (FaqReferenceSpec spec : request.references()) {
            FaqReference reference = new FaqReference();
            reference.setReferenceKind(spec.kind());
            reference.setReferenceValue(spec.value());
            faq.addReference(reference);
        }
        return toView(faqRepository.save(faq), contentView);
    }

    @Transactional(readOnly = true)
    public List<FaqView> list(StoreMerchantId store, Long groupId) throws ContentNotFoundException {
        List<FaqView> result = new ArrayList<>();
        for (ContentFaq faq : faqRepository
                .findAllByGroupIdAndContentStoreMerchantIdOrderByPositionAscIdAsc(groupId, store)) {
            result.add(toView(faq, contentService.find(store, faq.getId())));
        }
        return List.copyOf(result);
    }

    @Transactional
    public List<FaqView> reorder(StoreMerchantId store, FaqReorderRequest request)
            throws FaqGroupNotFoundException, InvalidFaqReorderException, ContentNotFoundException {
        groupRepository.findForUpdateByIdAndStoreMerchantId(request.groupId(), store)
                .orElseThrow(() -> FaqGroupNotFoundException.forId(request.groupId()));
        List<ContentFaq> entries = faqRepository
                .findAllByGroupIdAndContentStoreMerchantIdOrderByPositionAscIdAsc(request.groupId(), store);
        Set<Long> expected = new HashSet<>(entries.stream().map(ContentFaq::getId).toList());
        Set<Long> supplied = new HashSet<>(request.orderedFaqIds());
        if (expected.size() != entries.size() || supplied.size() != request.orderedFaqIds().size()
                || !expected.equals(supplied)) {
            throw InvalidFaqReorderException.because("membership");
        }
        Map<Long, ContentFaq> byId = entries.stream().collect(java.util.stream.Collectors.toMap(
                ContentFaq::getId, java.util.function.Function.identity()));
        List<FaqView> result = new ArrayList<>();
        for (int position = 0; position < request.orderedFaqIds().size(); position++) {
            ContentFaq faq = byId.get(request.orderedFaqIds().get(position));
            faq.setPosition(position + 100_000);
        }
        faqRepository.saveAllAndFlush(entries);
        for (int position = 0; position < request.orderedFaqIds().size(); position++) {
            ContentFaq faq = byId.get(request.orderedFaqIds().get(position));
            faq.setPosition(position);
            result.add(toView(faq, contentService.find(store, faq.getId())));
        }
        faqRepository.saveAll(entries);
        return List.copyOf(result);
    }

    private void shift(List<ContentFaq> entries, int from) {
        Map<Long, Integer> original = entries.stream().collect(java.util.stream.Collectors.toMap(
                ContentFaq::getId, ContentFaq::getPosition));
        entries.stream().filter(it -> it.getPosition() >= from)
                .forEach(it -> it.setPosition(it.getPosition() + 100_000));
        faqRepository.saveAllAndFlush(entries);
        entries.stream().filter(it -> original.get(it.getId()) >= from)
                .forEach(it -> it.setPosition(original.get(it.getId()) + 1));
        faqRepository.saveAll(entries);
    }

    private static FaqGroupView toView(FaqGroup group, LanguageCode language) {
        FaqGroupDescription description = group.getDescriptions().stream()
                .filter(it -> it.getLanguageCode().equals(language)).findFirst()
                .orElseGet(() -> group.getDescriptions().stream().findFirst().orElseThrow());
        return new FaqGroupView(group.getId(), group.getCode(), group.getPosition(),
                description.getLanguageCode(), description.getName());
    }

    private static FaqView toView(ContentFaq faq, ContentView content) {
        List<FaqReferenceSpec> references = faq.getReferences().stream()
                .map(it -> new FaqReferenceSpec(it.getReferenceKind(), it.getReferenceValue())).toList();
        return new FaqView(content, faq.getGroup().getId(), faq.getPosition(), references);
    }
}
