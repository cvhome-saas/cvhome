insert into router.pod(id, region, sub_region, pod_type, namespace, location, location_alis)
VALUES ('65f023632bc46470c204b78f', 'LOCAL', 'R1', 'INTERNAL', 'store-pod-1.cvhome.lcl',
        'http://store-pod-1.gateway.com:6001', 'http://store-pod-1.gateway.com:6001')
on conflict do nothing;

insert into router.reference(id, reference, enabled, pod_id)
values ('65f023632bc46470c204b76f', '65f023632bc46470c104b76f', true,
        '65f023632bc46470c204b78f')
on conflict do nothing;

insert into router.reference_alis(id, domain, reference_id)
values ('65f023632bc46470c204b75f', 'ashraf.com', '65f023632bc46470c204b76f')
on conflict do nothing;

insert into router.reference_alis(id, domain, reference_id)
values ('65f023632bc46470c204b25f', 'ali.com', '65f023632bc46470c204b76f')
on conflict do nothing;
