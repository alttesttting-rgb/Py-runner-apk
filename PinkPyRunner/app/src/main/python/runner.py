"""Execute selected Python scripts with local imports and live console output."""
import os
import sys
import traceback

class _ConsoleStream:
    def __init__(self, sink): self.sink = sink
    def write(self, value):
        if value: self.sink.onOutput(str(value))
        return len(value)
    def flush(self): pass
    def isatty(self): return False

def run_script(script_path, sink, control):
    old_stdout, old_stderr = sys.stdout, sys.stderr
    old_argv, old_path = sys.argv[:], sys.path[:]
    old_cwd = os.getcwd()
    stream = _ConsoleStream(sink)
    script_path = os.path.abspath(script_path)
    script_dir = os.path.dirname(script_path)

    def stop_trace(frame, event, arg):
        if control.isStopped():
            raise KeyboardInterrupt("Stopped by user")
        return stop_trace

    try:
        os.chdir(script_dir)
        sys.path.insert(0, script_dir)
        sys.argv = [script_path]
        sys.stdout = stream
        sys.stderr = stream
        sink.onOutput("[PinkPy] Starting script...\\n")
        with open(script_path, "r", encoding="utf-8-sig") as source:
            source_text = source.read()
        code = compile(source_text, script_path, "exec")
        namespace = {"__name__": "__main__", "__file__": script_path, "__package__": None}
        sys.settrace(stop_trace)
        exec(code, namespace, namespace)
        sink.onOutput("\\n[PinkPy] Script finished.\\n")
    except KeyboardInterrupt:
        sink.onOutput("\\n[PinkPy] Script stopped.\\n")
    except BaseException:
        traceback.print_exc(file=stream)
        sink.onOutput("\\n[PinkPy] Script failed. See error above.\\n")
    finally:
        sys.settrace(None)
        sys.stdout, sys.stderr = old_stdout, old_stderr
        sys.argv = old_argv
        sys.path[:] = old_path
        os.chdir(old_cwd)
