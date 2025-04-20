import { TestBed } from '@angular/core/testing';
import { HttpInterceptorFn, HttpRequest, HttpHandlerFn } from '@angular/common/http';

import { JwtInterceptor } from './jwt.interceptor';

describe('jwtInterceptor', () => {
  const interceptor: HttpInterceptorFn = (req: HttpRequest<any>, next: HttpHandlerFn) =>
    TestBed.runInInjectionContext(() => interceptor(req, next));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(interceptor).toBeTruthy();
  });
});
