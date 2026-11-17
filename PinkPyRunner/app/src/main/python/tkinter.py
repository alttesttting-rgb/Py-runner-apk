"""Small Android-native Tkinter compatibility layer for PinkPy Runner.

This is not desktop Tkinter. It implements a practical subset using Android dialogs.
Supported: Tk, Label, Button, Entry, Checkbutton, Scale, StringVar/IntVar,
pack/grid/place (basic layout no-ops), config/configure, get/set, and messagebox.
"""
from com.pinkpy.runner import AndroidTk

class Variable:
    def __init__(self, value=""): self.value = value
    def get(self): return self.value
    def set(self, value): self.value = value

class StringVar(Variable): pass
class IntVar(Variable):
    def __init__(self, value=0): super().__init__(int(value))
class BooleanVar(Variable):
    def __init__(self, value=False): super().__init__(bool(value))

class Widget:
    kind = "label"
    def __init__(self, master=None, **kwargs):
        self.master = master
        self.text = str(kwargs.get("text", kwargs.get("textvariable", "")))
        self.command = kwargs.get("command")
        self.variable = kwargs.get("variable")
        self.value = kwargs.get("value", self.variable.get() if self.variable else "")
        self.options = kwargs
        if self.variable is not None:
            try: self.text = str(self.variable.get())
            except Exception: pass
        if master is not None and hasattr(master, "_widgets"):
            master._widgets.append(self)
    def pack(self, *a, **k): return self
    def grid(self, *a, **k): return self
    def place(self, *a, **k): return self
    def config(self, **kwargs): self.configure(**kwargs)
    def configure(self, **kwargs):
        self.options.update(kwargs)
        if "text" in kwargs: self.text = str(kwargs["text"])
        if "command" in kwargs: self.command = kwargs["command"]
        return self
    def cget(self, key): return self.options.get(key, getattr(self, key, None))
    def destroy(self): pass
    def _set_value(self, value):
        self.value = value
        if self.variable is not None:
            try: self.variable.set(value)
            except Exception: pass
    def get(self): return self.value
    def insert(self, index, value): self._set_value(str(self.value) + str(value))
    def delete(self, start, end=None): self._set_value("")

class Tk:
    def __init__(self, *a, **k):
        self._widgets = []
        self._title = "Python GUI"
    def title(self, value): self._title = str(value)
    def geometry(self, value): pass
    def resizable(self, *a): pass
    def configure(self, **k): pass
    config = configure
    def mainloop(self):
        AndroidTk.showWindow(self._title, self._widgets)
    def update(self): pass
    def update_idletasks(self): pass
    def quit(self): pass
    def destroy(self): pass
    def withdraw(self): pass
    def after(self, ms, callback): callback()

class Toplevel(Tk):
    pass

class Frame(Widget):
    def __init__(self, master=None, **kwargs):
        self._widgets = []
        super().__init__(master, **kwargs)

class Label(Widget): kind = "label"
class Button(Widget): kind = "button"
class Entry(Widget): kind = "entry"
class Checkbutton(Widget): kind = "checkbutton"
class Scale(Widget): kind = "scale"
class Radiobutton(Widget): kind = "label"
class Text(Entry): pass

class OptionMenu(Widget):
    kind = "label"
    def __init__(self, master, variable, *values, **kwargs):
        super().__init__(master, text=str(variable.get()), **kwargs)
        self.variable = variable
        self.values = values

class _MessageBox:
    @staticmethod
    def showinfo(title, message): AndroidTk.showMessage(str(title), str(message)); return "ok"
    @staticmethod
    def showwarning(title, message): AndroidTk.showMessage(str(title), str(message)); return "ok"
    @staticmethod
    def showerror(title, message): AndroidTk.showMessage(str(title), str(message)); return "ok"
    @staticmethod
    def askyesno(title, message): AndroidTk.showMessage(str(title), str(message)); return True

messagebox = _MessageBox()

class Combobox(Widget):
    kind = "label"
    def __init__(self, master=None, **kwargs):
        self.values = list(kwargs.get("values", []))
        self.state = kwargs.get("state", "readonly")
        super().__init__(master, **kwargs)
        if self.values and not self.value:
            self.value = self.values[0]
        self.text = str(self.value)
    def current(self, index=None):
        if index is None:
            try: return self.values.index(self.get())
            except ValueError: return -1
        if 0 <= int(index) < len(self.values):
            self._set_value(self.values[int(index)])
    def set(self, value): self._set_value(value)
    def bind(self, sequence, callback): return None

class Style:
    def __init__(self, *a, **k): pass
    def theme_use(self, *a): pass
    def configure(self, *a, **k): pass
    def map(self, *a, **k): pass

class Separator(Widget): kind = "label"
class Progressbar(Widget): kind = "label"
class _TTK:
    Frame = Frame
    Label = Label
    Button = Button
    Entry = Entry
    Checkbutton = Checkbutton
    Radiobutton = Radiobutton
    Scale = Scale
    Combobox = Combobox
    Separator = Separator
    Progressbar = Progressbar
    Style = Style

ttk = _TTK()
