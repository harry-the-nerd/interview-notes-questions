import re

def flatten_result(result):
    return re.sub(r'[\s\r\n]+', ' ', result).strip()

def result_lines(result):
    return [x.strip() for x in re.split(r'\r?\n', re.sub(r' +', ' ', result)) if x.strip() != '']

def indicates_unbound_local_error(rendered_output, unbound_var):
    var = "&#39;{}&#39;".format(unbound_var)
    error_msgs = (
        # < 3.11
        "local variable {} referenced before assignment".format(var),
        # >= 3.11
        "cannot access local variable {} where it is not associated".format(var),
    )
    return any((msg in rendered_output) for msg in error_msgs)
