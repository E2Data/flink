import { ChangeDetectionStrategy, ChangeDetectorRef, Component, ViewChild, OnDestroy, OnInit } from '@angular/core';
import { Subject } from 'rxjs';
import { HaierService } from 'services';
import { MonacoEditorComponent } from 'share/common/monaco-editor/monaco-editor.component';

@Component({
  selector: 'haier-manager-logs',
  templateUrl: './haier-manager-logs.component.html',
  styleUrls: ['./haier-manager-logs.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HaierManagerLogsComponent implements OnInit, OnDestroy {
  destroy$ = new Subject();
  isLoading = false;
  logs = '';
  @ViewChild(MonacoEditorComponent) monacoEditorComponent: MonacoEditorComponent;

  private reloadLogs() {
    this.isLoading = true;
    this.haierService.loadLogs().subscribe(data => {
      console.log(data);
      this.logs = data.logs.join('\n');
      this.isLoading = false;
      this.layoutEditor();
      this.cdr.markForCheck();
    });
  }

  layoutEditor(): void {
    setTimeout(() => this.monacoEditorComponent.layout());
  }

  constructor(private haierService: HaierService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.reloadLogs();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
