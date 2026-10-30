import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClassesAdmin } from './classes-admin';

describe('ClassesAdmin', () => {
  let component: ClassesAdmin;
  let fixture: ComponentFixture<ClassesAdmin>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClassesAdmin]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClassesAdmin);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
