package kg.syntlex.message_connection;

public enum MessageType
{
    NAME_REQUEST,// запрос имени
    USER_NAME,//имя пользователя
    NAME_ACCEPTED,// имя принято
    TEXT,//текстовое сообщение
    USER_ADDED,//пользователь добавлен
    USER_REMOVED, //пользователь удален
    DISABLE_USER, //отключения пользователя
    NAME_USED,
    VOICE, // голосовое сообщение
    IMAGE, //картинки
    FILE // текстовые файлы
}