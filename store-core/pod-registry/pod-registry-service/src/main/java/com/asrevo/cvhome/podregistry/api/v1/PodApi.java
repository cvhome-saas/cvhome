package com.asrevo.cvhome.podregistry.api.v1;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.annotation.OrgStorePrincipalInfo;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.podregistry.commons.dto.PodView;
import com.asrevo.cvhome.podregistry.commons.errors.DuplicatePodNameException;
import com.asrevo.cvhome.podregistry.commons.errors.PodNotFoundException;
import com.asrevo.cvhome.podregistry.service.PodService;

import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The pod registry's HTTP surface.
 *
 * <p>
 * Reads are open to super admins, org admins and store-core service principals — the gateway is a service principal
 * and rebuilds its route table from {@code list} every minute, so locking reads to super admins would take down
 * tenant routing. Writes are super-admin only: a pod is infrastructure, not tenant data.
 * </p>
 *
 * <p>
 * The tokens take a {@code null} target — a pod is infrastructure, not tenant data, so there is no
 * {@code ManagerStoreId} to scope against. That is the same shape billing's {@code QUOTA-CHECK} uses. Row filtering
 * for an org admin happens in {@link #listPods}, not in the permission layer.
 * </p>
 */
@RestController
@RequestMapping("api/v1/pod")
@AllArgsConstructor
@Tag(name = "Pod registry", description = "Pods: the physical per-region deployments that host stores")
@Slf4j
public class PodApi {

    private static final String READ = "hasPermission(null,'PodId','STORE-CORE.POD.READ')";

    private static final String MANAGE = "hasPermission(null,'PodId','STORE-CORE.POD.MANAGE')";

    private final PodService podService;

    /**
     * Every pod, unpaged — the gateway's route source.
     *
     * <p>
     * An org admin sees only its own private pods; a super admin and a service principal see all. The identity
     * carries no org for either of the latter two, which is what distinguishes them here.
     * </p>
     */
    @GetMapping("list")
    @PreAuthorize(READ)
    public List<Pod> listPods(@OrgStorePrincipalInfo UserOrgStoreIdentity identity) {
        Pageable pageable = Pageable.unpaged();
        return isPlatformWide(identity) ? podService.listAllPods(pageable).toList()
                : podService.listAllPods(identity.org(), pageable).toList();
    }

    /**
     * Whether the caller sees every org's pods: a super admin or a {@code store_core} service principal, both of
     * which arrive with no org claim. The gateway and tenancy are service principals, so both see all.
     */
    private static boolean isPlatformWide(UserOrgStoreIdentity identity) {
        return identity == null || identity.org() == null || identity.org().id() == null;
    }

    /**
     * The same pods as {@link #listPods}, paged — what the super-admin pod screen binds to.
     *
     * <p>
     * Scoped identically: an org admin sees only its own private pods. It exists alongside the unpaged {@code list}
     * because the gateway wants every route in one call and a table wants a page; folding them together would make
     * one of the two callers wrong.
     * </p>
     */
    @GetMapping
    @PreAuthorize(READ)
    public Page<Pod> findAllPods(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, Pageable pageable) {
        return isPlatformWide(identity) ? podService.listAllPods(pageable)
                : podService.listAllPods(identity.org(), pageable);
    }

    /** The registry's own view, including lifecycle, capacity and health. Super admin only. */
    @GetMapping("{id}")
    @PreAuthorize(MANAGE)
    public PodView find(@PathVariable PodId id) throws PodNotFoundException {
        return podService.view(id);
    }

    @PostMapping
    @PreAuthorize(MANAGE)
    public Pod create(@RequestBody Pod pod) throws DuplicatePodNameException {
        return podService.save(pod);
    }

    @PutMapping("{id}")
    @PreAuthorize(MANAGE)
    public Pod update(@PathVariable PodId id, @RequestBody Pod pod)
            throws PodNotFoundException, DuplicatePodNameException {
        return podService.update(id, pod);
    }

    /**
     * Removes a pod from the registry.
     *
     * <p>
     * It does <strong>not</strong> check whether stores are still placed here, and there is no foreign key to stop
     * it — tenancy owns {@code manager_store.pod_id} in a different schema. Deleting a populated pod orphans every
     * store on it. Drain, in phase 8, is the safe operation; until then this is a sharp tool and super-admin only.
     * </p>
     */
    @DeleteMapping("{id}")
    @PreAuthorize(MANAGE)
    public void delete(@PathVariable PodId id) throws PodNotFoundException {
        podService.delete(id);
    }

}
