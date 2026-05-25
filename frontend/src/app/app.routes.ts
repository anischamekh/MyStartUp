import { Routes } from '@angular/router';
import { authGuard } from './services/auth.guard';
import { LoginComponent } from './components/login/login.component';
import { MainLayoutComponent } from './components/main-layout/main-layout.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { EmployeesComponent } from './components/employees/employees.component';
import { TeamsComponent } from './components/teams/teams.component';
import { TasksComponent } from './components/tasks/tasks.component';
import { LeavesComponent } from './components/leaves/leaves.component';
import { NotificationsComponent } from './components/notifications/notifications.component';
import { ProjectsComponent } from './components/projects/projects.component';
import { DocumentsComponent } from './components/documents/documents.component';
import { PayrollComponent } from './components/payroll/payroll.component';
import { ReportsComponent } from './components/reports/reports.component';
import { SkillsTrainingComponent } from './components/skills-training/skills-training.component';
import { EvaluationsComponent } from './components/evaluations/evaluations.component';
import { LeaveCalendarComponent } from './components/leave-calendar/leave-calendar.component';
export const routes: Routes = [
  { path: 'login', component: LoginComponent },

  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', component: DashboardComponent, data: { title: 'Dashboard' } },
      {
        path: 'employees',
        component: EmployeesComponent,
        canActivate: [authGuard],
        data: { title: 'Employees', roles: ['HR', 'ADMIN'] }
      },
      {
        path: 'teams',
        component: TeamsComponent,
        canActivate: [authGuard],
        data: { title: 'Teams', roles: ['HR', 'ADMIN'] }
      },
      {
        path: 'tasks',
        component: TasksComponent,
        canActivate: [authGuard],
        data: { title: 'Tasks', roles: ['EMPLOYEE', 'TEAM_LEADER', 'ADMIN'] }
      },
      {
        path: 'projects',
        component: ProjectsComponent,
        canActivate: [authGuard],
        data: { title: 'Projects', roles: ['MANAGER', 'ADMIN'] }
      },
      {
        path: 'leaves',
        component: LeavesComponent,
        canActivate: [authGuard],
        data: { title: 'Leave requests', roles: ['EMPLOYEE', 'TEAM_LEADER', 'MANAGER', 'HR', 'ADMIN'] }
      },
      { path: 'documents', component: DocumentsComponent, data: { title: 'Documents' } },
      {
        path: 'payroll',
        component: PayrollComponent,
        canActivate: [authGuard],
        data: { title: 'Payroll', roles: ['EMPLOYEE', 'HR', 'ADMIN'] }
      },
      {
        path: 'reports',
        component: ReportsComponent,
        canActivate: [authGuard],
        data: { title: 'Reports', roles: ['MANAGER', 'ADMIN'] }
      },
      {
        path: 'evaluations',
        component: EvaluationsComponent,
        canActivate: [authGuard],
        data: { title: 'Evaluations', roles: ['TEAM_LEADER', 'HR', 'MANAGER', 'ADMIN'] }
      },
      {
        path: 'leave-calendar',
        component: LeaveCalendarComponent,
        canActivate: [authGuard],
        data: { title: 'Leave calendar', roles: ['HR', 'MANAGER', 'ADMIN'] }
      },
      { path: 'skills-training', component: SkillsTrainingComponent, data: { title: 'Skills & training' } },
      { path: 'notifications', component: NotificationsComponent, data: { title: 'Notifications' } }
    ]
  },

  { path: '**', redirectTo: 'dashboard' }
];
