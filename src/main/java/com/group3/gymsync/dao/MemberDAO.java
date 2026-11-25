package com.group3.gymsync.dao;

import gymsync.database.DatabaseConnection;
import gymsync.model.Member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MemberDAO {
    private static final String INSERT_MEMBER = """
            INSERT INTO member (first_name, last_name, contact_number, email, address, date_registered, status)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String UPDATE_MEMBER = """
            UPDATE member
            SET first_name = ?, last_name = ?, contact_number = ?, email = ?, address = ?, date_registered = ?, status = ?
            WHERE member_id = ?
            """;
    private static final String SELECT_MEMBER_BY_ID = "SELECT * FROM member WHERE member_id = ?";
    private static final String SELECT_ALL_MEMBERS = "SELECT * FROM member ORDER BY member_id";

    private final DatabaseConnection databaseConnection;

    public MemberDAO() {
        this(new DatabaseConnection());
    }

    public MemberDAO(DatabaseConnection databaseConnection) {
        this.databaseConnection = Objects.requireNonNull(databaseConnection, "databaseConnection");
    }

    public int create(Member member) throws SQLException {
        Objects.requireNonNull(member, "member");
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_MEMBER, Statement.RETURN_GENERATED_KEYS)) {
            bindMemberFields(statement, member);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("Creating a member did not return a generated member ID.");
                }
                int memberId = generatedKeys.getInt(1);
                member.setMemberId(memberId);
                return memberId;
            }
        }
    }

    public boolean update(Member member) throws SQLException {
        Objects.requireNonNull(member, "member");
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_MEMBER)) {
            bindMemberFields(statement, member);
            statement.setInt(8, member.getMemberId());
            return statement.executeUpdate() == 1;
        }
    }

    public Optional<Member> findById(int memberId) throws SQLException {
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_MEMBER_BY_ID)) {
            statement.setInt(1, memberId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapMember(resultSet)) : Optional.empty();
            }
        }
    }

    public List<Member> findAll() throws SQLException {
        List<Member> members = new ArrayList<>();
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL_MEMBERS);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                members.add(mapMember(resultSet));
            }
        }
        return members;
    }

    private static void bindMemberFields(PreparedStatement statement, Member member) throws SQLException {
        statement.setString(1, member.getFirstName());
        statement.setString(2, member.getLastName());
        statement.setString(3, member.getContactNumber());
        statement.setString(4, member.getEmail());
        statement.setString(5, member.getAddress());
        statement.setString(6, member.getDateRegistered());
        statement.setString(7, member.getStatus());
    }

    private static Member mapMember(ResultSet resultSet) throws SQLException {
        return new Member(
                resultSet.getInt("member_id"),
                resultSet.getString("first_name"),
                resultSet.getString("last_name"),
                resultSet.getString("contact_number"),
                resultSet.getString("email"),
                resultSet.getString("address"),
                resultSet.getString("date_registered"),
                resultSet.getString("status"));
    }
}