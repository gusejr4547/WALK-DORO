import 'package:json_annotation/json_annotation.dart';

part 'user.g.dart';

@JsonSerializable(fieldRename: FieldRename.snake)
class User {
  final int id;
  final String email;
  final String nickname;
  final int points;
  final int stepCount;

  const User({
    required this.id,
    required this.email,
    required this.nickname,
    required this.points,
    required this.stepCount,
  });

  factory User.fromJson(Map<String, dynamic> json) => _$UserFromJson(json);
  Map<String, dynamic> toJson() => _$UserToJson(this);
}
