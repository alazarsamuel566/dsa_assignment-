left_stack = []
right_stack = []

def type_char(c):
    left_stack.append(c)

def move_left():
    if left_stack:
        right_stack.append(left_stack.pop())

def move_right():
    if right_stack:
        left_stack.append(right_stack.pop())

def backspace():
    if left_stack:
        left_stack.pop()

def delete():
    if right_stack:
        right_stack.pop()

def show():
    print("".join(left_stack) + "|" + "".join(reversed(right_stack)))

while True:
    cmd = input("> ").strip()

    if cmd == "EXIT":
        break
    elif cmd == "LEFT":
        move_left()
    elif cmd == "RIGHT":
        move_right()
    elif cmd == "BACKSPACE":
        backspace()
    elif cmd == "DELETE":
        delete()
    elif cmd == "SHOW":
        show()
    elif cmd.startswith("TYPE "):
        type_char(cmd[5:])
    else:
        print("Invalid command")
