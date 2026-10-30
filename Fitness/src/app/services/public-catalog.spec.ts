import { TestBed } from '@angular/core/testing';

import { PublicCatalog } from './public-catalog';

describe('PublicCatalog', () => {
  let service: PublicCatalog;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PublicCatalog);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
