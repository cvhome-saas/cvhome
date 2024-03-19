

insert into router.pod(id, region, sub_region, pod_type, namespace, location, location_alis)
VALUES ('65f023632bc46470c204b78f', 'EU_CENTRAL_1','R1','INTERNAL','store-pod-1.cvhome.lcl','https://loadbalancer-name.com','https://domain-alis.com')
on conflict do nothing;

