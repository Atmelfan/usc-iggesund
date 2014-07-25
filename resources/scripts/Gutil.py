def gui_yesno(x, y, title, message):
    t = ""
    length = 0
    for s in message:
        t += "|%s\n" % s
        length = max(length, len(s))
    length -= len(title)+1
    title += "-"*length
    return """\x1b[%(x)d;%(y)dH
    |%(title)s
    %(message)s
    \\\t[  Yes  ]\t[  No  ]""" % {'x': x, 'y': y, 'title': title, 'message': t}
#   |Confirm server connect?
#   |Start menu wants to redirect you
#   |to 127.0.0.1, continue?
#   \   [Yes    ]   [No     ]