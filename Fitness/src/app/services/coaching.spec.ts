import { TestBed } from '@angular/core/testing';

import { Coaching } from './coaching';

describe('Coaching', () => {
  let service: Coaching;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Coaching);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
