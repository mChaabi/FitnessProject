import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UnsubscribePage } from './unsubscribe-page';

describe('UnsubscribePage', () => {
  let component: UnsubscribePage;
  let fixture: ComponentFixture<UnsubscribePage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UnsubscribePage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UnsubscribePage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
