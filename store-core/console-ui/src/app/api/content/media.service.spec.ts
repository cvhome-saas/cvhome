import {HttpEventType} from '@angular/common/http';

import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {MediaService} from './media.service';

describe('MediaService', () => {
  let service: MediaService;
  let http: ReturnType<typeof apiHarness<MediaService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(MediaService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const BASE = '/spg/content/api/v1/private/content/media';

  it('lists with the library filters', () => {
    service.list({folder: 4, kind: 'IMAGE', q: 'hero', used: false, page: 0, count: 24}).subscribe();
    const request = http.expectOne((r) => r.url === BASE);
    expect(request.request.params.get('folder')).toBe('4');
    expect(request.request.params.get('kind')).toBe('IMAGE');
    expect(request.request.params.get('used')).toBe('false');
    expect(request.request.params.get('store')).toBe(TEST_STORE);
    request.flush({content: [], totalElements: 0, totalPages: 0, pageNumber: 0, size: 24});
  });

  it('uploads every file as a `files` part and reports progress then the assets', () => {
    const seen: string[] = [];
    const file = new File(['x'], 'x.png', {type: 'image/png'});
    service.upload([file, file], 2).subscribe((event) => seen.push(event.kind));
    const request = http.expectOne((r) => r.url === BASE && r.method === 'POST');
    expect(request.request.body instanceof FormData).toBeTrue();
    expect((request.request.body as FormData).getAll('files').length).toBe(2);
    expect(request.request.params.get('folderId')).toBe('2');
    request.event({type: HttpEventType.UploadProgress, loaded: 5, total: 10});
    request.flush([{id: 1}]);
    expect(seen).toEqual(['progress', 'done']);
  });

  it('patches metadata, deletes with force, and manages folders', () => {
    service.patch(9, {title: 'Hero'}).subscribe();
    http.expectOne((r) => r.url === `${BASE}/9` && r.method === 'PATCH').flush({});

    service.delete(9, true).subscribe();
    const deleted = http.expectOne((r) => r.url === `${BASE}/9` && r.method === 'DELETE');
    expect(deleted.request.params.get('force')).toBe('true');
    deleted.flush(null);

    service.folders().subscribe();
    http.expectOne((r) => r.url === `${BASE}/folders`).flush([]);

    service.deleteFolder(3, 1).subscribe();
    const folder = http.expectOne((r) => r.url === `${BASE}/folders/3`);
    expect(folder.request.params.get('moveTo')).toBe('1');
    folder.flush(null);
  });
});
