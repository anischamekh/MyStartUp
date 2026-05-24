import { Component, OnInit, OnDestroy, inject, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet, NavigationEnd, ActivatedRouteSnapshot } from '@angular/router';
import { filter, Subscription } from 'rxjs';

import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';

import { AuthService } from '../../services/auth.service';
import { LayoutService } from '../../services/layout.service';
import { ThemeService } from '../../services/theme.service';
import { NotificationBadgeService } from '../../services/notification-badge.service';
import type { RoleName } from '../../models/role-name.model';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, ButtonModule, ToastModule],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.css'
})
export class MainLayoutComponent implements OnInit, OnDestroy {
  readonly auth = inject(AuthService);
  readonly layout = inject(LayoutService);
  readonly theme = inject(ThemeService);
  readonly badge = inject(NotificationBadgeService);
  private readonly router = inject(Router);

  pageTitle = 'Dashboard';
  private sub?: Subscription;

  ngOnInit(): void {
    this.layout.setMobile(this.checkMobile());
    this.updateTitle();
    this.badge.refresh();

    this.sub = this.router.events.pipe(filter((e) => e instanceof NavigationEnd)).subscribe(() => {
      this.updateTitle();
      this.badge.refresh();
      this.layout.closeMobileDrawer();
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  @HostListener('window:resize')
  onResize(): void {
    this.layout.setMobile(this.checkMobile());
  }

  private checkMobile(): boolean {
    return window.innerWidth < 960;
  }

  private updateTitle(): void {
    const snap = this.router.routerState.snapshot.root;
    const deepest = this.getDeepestChild(snap);
    const title = (deepest.data['title'] as string) ?? 'EMS';
    this.pageTitle = title;
  }

  private getDeepestChild(route: ActivatedRouteSnapshot): ActivatedRouteSnapshot {
    let r = route;
    while (r.firstChild) {
      r = r.firstChild;
    }
    return r;
  }

  logout(): void {
    this.auth.logout();
    void this.router.navigateByUrl('/login');
  }

  toggleSidebar(): void {
    this.layout.toggleSidebar();
  }

  showNavLink(roles: RoleName[] | null): boolean {
    if (!roles || roles.length === 0) return true;
    const role = this.auth.role;
    return !!role && roles.includes(role);
  }

  goNotifications(): void {
    void this.router.navigate(['/notifications']);
  }
}
