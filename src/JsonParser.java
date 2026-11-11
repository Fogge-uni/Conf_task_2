import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonParser {
    private String json;
    private int position;

    public Object parse(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("JSON-файл пуст");
        }

        this.json = json;
        this.position = 0;

        Object result = parseValue();
        skipWhitespace();

        if (position != json.length()) {
            throw new IllegalArgumentException("Лишние данные после JSON");
        }

        return result;
    }

    private Object parseValue() {
        skipWhitespace();

        if (position >= json.length()) {
            throw new IllegalArgumentException("Неожиданный конец JSON");
        }

        char current = json.charAt(position);

        switch (current) {
            case '{':
                return parseObject();
            case '[':
                return parseArray();
            case '"':
                return parseString();
            case 't':
                return parseLiteral("true", Boolean.TRUE);
            case 'f':
                return parseLiteral("false", Boolean.FALSE);
            case 'n':
                return parseLiteral("null", null);
            default:
                if (current == '-' || isDigit(current)) {
                    return parseNumber();
                }
                throw new IllegalArgumentException(
                        "Некорректное значение JSON в позиции " + position);
        }
    }

    private Map<String, Object> parseObject() {
        Map<String, Object> result = new LinkedHashMap<>();
        position++;
        skipWhitespace();

        if (takeCharacter('}')) {
            return result;
        }

        while (true) {
            skipWhitespace();

            if (position >= json.length() || json.charAt(position) != '"') {
                throw new IllegalArgumentException(
                        "Ожидалось имя свойства JSON");
            }

            String key = parseString();
            skipWhitespace();
            expectCharacter(':');

            Object value = parseValue();
            result.put(key, value);

            skipWhitespace();

            if (takeCharacter('}')) {
                return result;
            }

            expectCharacter(',');
        }
    }

    private List<Object> parseArray() {
        List<Object> result = new ArrayList<>();
        position++;
        skipWhitespace();

        if (takeCharacter(']')) {
            return result;
        }

        while (true) {
            result.add(parseValue());
            skipWhitespace();

            if (takeCharacter(']')) {
                return result;
            }

            expectCharacter(',');
        }
    }

    private String parseString() {
        expectCharacter('"');
        StringBuilder result = new StringBuilder();

        while (position < json.length()) {
            char current = json.charAt(position++);

            if (current == '"') {
                return result.toString();
            }

            if (current == '\\') {
                appendEscapeSequence(result);
            } else {
                appendRegularCharacter(result, current);
            }
        }

        throw new IllegalArgumentException("Незакрытая строка JSON");
    }

    private void appendEscapeSequence(StringBuilder result) {
        if (position >= json.length()) {
            throw new IllegalArgumentException(
                    "Некорректная escape-последовательность");
        }

        char escaped = json.charAt(position++);

        switch (escaped) {
            case '"':
            case '\\':
            case '/':
                result.append(escaped);
                break;
            case 'b':
                result.append('\b');
                break;
            case 'f':
                result.append('\f');
                break;
            case 'n':
                result.append('\n');
                break;
            case 'r':
                result.append('\r');
                break;
            case 't':
                result.append('\t');
                break;
            case 'u':
                result.append(parseUnicode());
                break;
            default:
                throw new IllegalArgumentException(
                        "Неизвестная escape-последовательность");
        }
    }

    private void appendRegularCharacter(
            StringBuilder result, char current) {

        if (current < 0x20) {
            throw new IllegalArgumentException(
                    "Недопустимый символ в JSON-строке");
        }

        result.append(current);
    }

    private char parseUnicode() {
        if (position + 4 > json.length()) {
            throw new IllegalArgumentException(
                    "Некорректная Unicode-последовательность");
        }

        String hex = json.substring(position, position + 4);
        position += 4;

        try {
            return (char) Integer.parseInt(hex, 16);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Некорректный Unicode-символ в JSON");
        }
    }

    private Object parseNumber() {
        int start = position;

        takeCharacter('-');
        readIntegerPart();
        readFractionPart();
        readExponentPart();

        String number = json.substring(start, position);

        try {
            return Double.parseDouble(number);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Некорректное число в JSON");
        }
    }

    private void readIntegerPart() {
        if (takeCharacter('0')) {
            if (position < json.length() && isDigit(json.charAt(position))) {
                throw new IllegalArgumentException(
                        "В JSON число не может начинаться с нуля");
            }
            return;
        }

        if (position >= json.length() || !isDigit(json.charAt(position))) {
            throw new IllegalArgumentException("Некорректное число в JSON");
        }

        readDigits();
    }

    private void readFractionPart() {
        if (takeCharacter('.')) {
            if (position >= json.length() || !isDigit(json.charAt(position))) {
                throw new IllegalArgumentException(
                        "После точки в JSON-числе должна быть цифра");
            }

            readDigits();
        }
    }

    private void readExponentPart() {
        if (takeCharacter('e') || takeCharacter('E')) {
            takeCharacter('+');
            takeCharacter('-');

            if (position >= json.length() || !isDigit(json.charAt(position))) {
                throw new IllegalArgumentException(
                        "Некорректная степень числа в JSON");
            }

            readDigits();
        }
    }

    private void readDigits() {
        while (position < json.length() && isDigit(json.charAt(position))) {
            position++;
        }
    }

    private boolean isDigit(char value) {
        return value >= '0' && value <= '9';
    }

    private Object parseLiteral(String literal, Object value) {
        if (!json.startsWith(literal, position)) {
            throw new IllegalArgumentException(
                    "Некорректное значение JSON");
        }

        position += literal.length();
        return value;
    }

    private void skipWhitespace() {
        while (position < json.length()
                && (json.charAt(position) == ' '
                || json.charAt(position) == '\n'
                || json.charAt(position) == '\r'
                || json.charAt(position) == '\t')) {
            position++;
        }
    }

    private boolean takeCharacter(char expected) {
        if (position < json.length()
                && json.charAt(position) == expected) {
            position++;
            return true;
        }
        return false;
    }

    private void expectCharacter(char expected) {
        skipWhitespace();

        if (!takeCharacter(expected)) {
            throw new IllegalArgumentException(
                    "Ожидался символ '" + expected
                            + "' в позиции " + position);
        }
    }
}
