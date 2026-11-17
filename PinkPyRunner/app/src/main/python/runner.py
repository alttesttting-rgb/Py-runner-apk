"""Small runner module called by the Android Java activity."""
import sys
import traceback


class _ConsoleStream:
    def __init__(self, sink):
        self.sink = sink

    def write(self, value):
        if value:
            self.sink.onOutput(str(value))
        return len(value)

    def flush(self):
        pass

    def isatty(self):
        return False


def run_script(script_path, sink, control):
    """Execute one selected script and forward stdout/stderr to Android."""
    old_stdout, old_stderr = sys.stdout, sys.stderr
    stream = _ConsoleStream(sink)

    def stop_trace(frame, event, arg):
        if control.isStopped():
            raise KeyboardInterrupt("Stopped by user")
        return stop_trace

    try:
        sys.stdout = stream
        sys.stderr = stream
        sink.onOutput("[PinkPy] Starting script...\n")
        with open(script_path, "r", encoding="utf-8-sig") as source:
            source_text = source.read()
        code = compile(source_text, script_path, "exec")
        namespace = {
            "__name__": "__main__",
            "__file__": script_path,
            "__package__": None,
        }
        sys.argv = [script_path]
        sys.settrace(stop_trace)
        exec(code, namespace, namespace)
        sys.settrace(None)
        sink.onOutput("\n[PinkPy] Script finished.\n")
    except KeyboardInterrupt:
        sys.settrace(None)
        sink.onOutput("\n[PinkPy] Script stopped.\n")
    except BaseException:
        sys.settrace(None)
        traceback.print_exc(file=stream)
        sink.onOutput("\n[PinkPy] Script failed. See error above.\n")
    finally:
        sys.settrace(None)
        sys.stdout, sys.stderr = old_stdout, old_stderr
